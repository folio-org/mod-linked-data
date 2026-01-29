package org.folio.linked.data.service.reference;

import static java.lang.Long.parseLong;
import static org.folio.linked.data.util.Constants.MSG_NOT_FOUND_IN;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.linked.data.util.ResourceUtils;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReferenceServiceImpl implements ReferenceService {

  private final ResourceRepository resourceRepo;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourceMarcAuthorityService marcAuthorityService;

  @Override
  public Resource resolveReference(Reference reference) {
    if (reference.getId() != null) {
      return fetchResourceFromRepo(parseLong(reference.getId()));
    }

    if (reference.getSrsId() != null) {
      return resourceRepo.findByFolioMetadataSrsId(reference.getSrsId())
        .orElseGet(() -> marcAuthorityService.importResourceFromSrs(reference.getSrsId()));
    }

    if (reference.getRdfLink() != null) {
      // TODO: MODLD-968 - Fetch RDF from URL and then convert to Resource by using rdf4ld library
    }

    throw exceptionBuilder.badRequestException(
      "Invalid Reference",
      "Reference must contain at least one identifier: id, srsId, or rdfLink"
    );
  }

  private Resource fetchResourceFromRepo(Long id) {
    return resourceRepo.findById(id)
      .map(ResourceUtils::ensureLatestReplaced)
      .orElseThrow(() -> resourceNotFoundException(id));
  }

  private RequestProcessingException resourceNotFoundException(Long resourceId) {
    log.error(MSG_NOT_FOUND_IN, "Resource", "id", resourceId, "Folio graph");
    return exceptionBuilder.notFoundLdResourceByIdException("--", resourceId.toString());
  }
}
