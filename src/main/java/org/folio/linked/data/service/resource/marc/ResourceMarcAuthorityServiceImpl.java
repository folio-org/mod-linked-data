package org.folio.linked.data.service.resource.marc;

import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.linked.data.domain.dto.AssignmentCheckResponseDto.InvalidAssignmentReasonEnum.NOT_VALID_FOR_TARGET;
import static org.folio.linked.data.domain.dto.AssignmentCheckResponseDto.InvalidAssignmentReasonEnum.NO_LCCN;
import static org.folio.linked.data.domain.dto.AssignmentCheckResponseDto.InvalidAssignmentReasonEnum.UNSUPPORTED_MARC;
import static org.folio.linked.data.util.Constants.MSG_NOT_FOUND_IN;
import static org.folio.linked.data.util.JsonUtils.writeValueAsString;
import static org.folio.linked.data.util.LccnUtils.hasLccn;
import static org.folio.linked.data.util.ResourceUtils.setPreferred;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.client.SrsClient;
import org.folio.linked.data.domain.dto.AssignmentCheckResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.dto.Identifiable;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.util.ResourceUtils;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceMarcAuthorityServiceImpl implements ResourceMarcAuthorityService {

  private final SrsClient srsClient;
  private final ObjectMapper objectMapper;
  private final ResourceRepository resourceRepo;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final MarcAuthority2ldMapper marcAuthority2ldMapper;
  private final FolioMetadataRepository folioMetadataRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public Resource fetchAuthorityOrCreateFromSrsRecord(Identifiable identifiable) {
    return fetchResourceFromRepo(identifiable)
      .orElseGet(() -> createResourceFromSrs(identifiable.getSrsId()));
  }

  @Override
  public AssignmentCheckResponseDto validateAuthorityAssignment(String marc, AssignAuthorityTarget target) {
    if (!hasLccn(marc)) {
      return new AssignmentCheckResponseDto(false).invalidAssignmentReason(NO_LCCN);
    }

    return marcAuthority2ldMapper.fromMarcJson(marc)
      .stream()
      .findFirst()
      .map(authority -> checkCompatibilityWithTarget(authority, target))
      .orElseGet(() -> new AssignmentCheckResponseDto(false).invalidAssignmentReason(UNSUPPORTED_MARC));
  }

  private AssignmentCheckResponseDto checkCompatibilityWithTarget(org.folio.ld.dictionary.model.Resource authority,
                                                                  AssignAuthorityTarget target) {
    boolean isCompatible = target.isCompatibleWith(authority.getTypes());
    return new AssignmentCheckResponseDto(isCompatible)
      .invalidAssignmentReason(isCompatible ? null : NOT_VALID_FOR_TARGET);
  }

  private Optional<Resource> fetchResourceFromRepo(Identifiable identifiable) {
    return Optional.ofNullable(identifiable.getId())
      .flatMap(id -> resourceRepo.findById(parseLong(id)))
      .map(ResourceUtils::ensureLatestReplaced)
      .or(() -> Optional.ofNullable(identifiable.getSrsId())
        .flatMap(resourceRepo::findByFolioMetadataSrsId));
  }

  private Resource createResourceFromSrs(String srsId) {
    try {
      return ofNullable(srsClient.getSourceStorageRecordBySrsId(srsId))
        .flatMap(this::contentAsJsonString)
        .flatMap(this::firstAuthorityToEntity)
        .map(resourceGraphService::saveMergingGraph)
        .orElseThrow(() -> notFoundException(srsId));
    } catch (FeignException.NotFound e) {
      throw notFoundException(srsId);
    }
  }

  private Optional<String> contentAsJsonString(ResponseEntity<Record> response) {
    return ofNullable(response.getBody())
      .map(Record::getParsedRecord)
      .map(ParsedRecord::getContent)
      .map(c -> writeValueAsString(c, objectMapper));
  }

  private Optional<Resource> firstAuthorityToEntity(String marcJson) {
    return ofNullable(marcJson)
      .map(marcAuthority2ldMapper::fromMarcJson)
      .flatMap(resources -> resources.stream().findFirst())
      .map(resourceModelMapper::toEntity)
      .filter(Resource::isAuthority);
  }

  private RequestProcessingException notFoundException(String srsId) {
    log.error(MSG_NOT_FOUND_IN, "Source Record", "srsId", srsId, "SRS");
    return exceptionBuilder.notFoundSourceRecordException("srsId", srsId);
  }

  @Override
  public Long saveMarcAuthority(org.folio.ld.dictionary.model.Resource modelResource) {
    var mapped = resourceModelMapper.toEntity(modelResource);
    validateResource(mapped);
    if (sameResourceExistsByIdAndSrsId(mapped)) {
      return updateAuthority(mapped);
    }
    if (resourceExistsBySrsId(mapped)) {
      return replaceAuthority(mapped);
    }
    return createAuthority(mapped);
  }

  private static void validateResource(Resource resource) {
    if (!resource.isAuthority()) {
      logAndThrow("Resource is not an authority");
    }

    if (resource.getFolioMetadata() == null || resource.getFolioMetadata().getSrsId() == null) {
      logAndThrow("SRS ID is missing in the resource");
    }
  }

  private static void logAndThrow(String message) {
    log.error(message);
    throw new IllegalArgumentException(message);
  }

  private boolean sameResourceExistsByIdAndSrsId(Resource resource) {
    var id = resource.getId();
    var srsId  = resource.getFolioMetadata().getSrsId();
    return resourceRepo.existsById(id) && getIdBySrsId(srsId).filter(id::equals).isPresent();
  }

  private Optional<Long> getIdBySrsId(String srsId) {
    return folioMetadataRepository.findIdBySrsId(srsId)
      .map(FolioMetadataRepository.IdOnly::getId);
  }

  private boolean resourceExistsBySrsId(Resource resource) {
    return folioMetadataRepository.existsBySrsId(resource.getFolioMetadata().getSrsId());
  }

  private Long updateAuthority(Resource resource) {
    var id = resource.getId();
    var srsId = resource.getFolioMetadata().getSrsId();
    logMarcAction(resource, "found by id [" + id + "] with srsId [" + srsId + "]", "be updated");
    return saveAndPublishEvent(resource, ResourceUpdatedEvent::new);
  }

  private Long replaceAuthority(Resource resource) {
    var srsId = resource.getFolioMetadata().getSrsId();
    return resourceRepo.findByFolioMetadataSrsId(srsId)
      .map(previous -> {
        var previousObsolete = markObsolete(previous);
        setPreferred(resource, true);
        var re = new ResourceEdge(previousObsolete, resource, REPLACED_BY);
        previousObsolete.addOutgoingEdge(re);
        resource.addIncomingEdge(re);
        logMarcAction(resource, "not found by id, but found by srsId [" + srsId + "]",
          "be saved as a new version of previously existed resource [id " + previous.getId() + "]");
        return saveAndPublishEvent(resource, saved -> new ResourceReplacedEvent(previousObsolete, saved.getId()));
      })
      .orElseThrow(() -> notFoundException(srsId));
  }

  private Long createAuthority(Resource resource) {
    logMarcAction(resource, "not found by id and srsId", "be created");
    return saveAndPublishEvent(resource, ResourceCreatedEvent::new);
  }

  private void logMarcAction(Resource resource, String existence, String action) {
    log.info("Incoming Authority resource [id {}, srsId {}] is {} and will {}",
      resource.getId(), resource.getFolioMetadata().getSrsId(), existence, action);
  }

  private Long saveAndPublishEvent(Resource resource, Function<Resource, ResourceEvent> resourceEventSupplier) {
    var newResource = resourceGraphService.saveMergingGraph(resource);
    var event = resourceEventSupplier.apply(newResource);
    applicationEventPublisher.publishEvent(event);
    return newResource.getId();
  }

  private Resource markObsolete(Resource resource) {
    resource.setActive(false);
    setPreferred(resource, false);
    resource.setFolioMetadata(null);
    return resource;
  }
}
