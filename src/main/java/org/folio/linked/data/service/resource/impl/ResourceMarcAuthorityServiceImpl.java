package org.folio.linked.data.service.resource.impl;

import static java.util.Objects.isNull;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.ResourceGraphService;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceMarcAuthorityServiceImpl implements ResourceMarcAuthorityService {

  private final ObjectMapper objectMapper;
  private final ResourceRepository resourceRepo;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public Long saveMarcResource(org.folio.ld.dictionary.model.Resource modelResource) {
    var mapped = resourceModelMapper.toEntity(modelResource);
    if (!mapped.isAuthority()) {
      var message = "Resource is not an authority";
      log.error(message);
      throw new IllegalArgumentException(message);
    }
    if (resourceRepo.existsById(modelResource.getId())) {
      return updateAuthority(mapped);
    }
    if (folioMetadataRepository.existsBySrsId(modelResource.getFolioMetadata().getSrsId())) {
      return replaceAuthority(mapped);
    }
    return createAuthority(mapped);
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
        return saveAndPublishEvent(resource, saved -> new ResourceReplacedEvent(previousObsolete, saved));
      })
      .orElseThrow(() -> new NotFoundException("Resource not found by srsId: " + srsId));
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
    if (event instanceof ResourceReplacedEvent rre) {
      resourceRepo.save(rre.previous());
    }
    applicationEventPublisher.publishEvent(event);
    return newResource.getId();
  }

  private Resource markObsolete(Resource resource) {
    resource.setActive(false);
    setPreferred(resource, false);
    resource.setFolioMetadata(null);
    return resource;
  }

  private void setPreferred(Resource resource, boolean preferred) {
    if (isNull(resource.getDoc())) {
      resource.setDoc(objectMapper.createObjectNode());
    }
    var arrayNode = objectMapper.createArrayNode().add(String.valueOf(preferred));
    ((ObjectNode) resource.getDoc()).set(RESOURCE_PREFERRED.getValue(), arrayNode);
  }
}
