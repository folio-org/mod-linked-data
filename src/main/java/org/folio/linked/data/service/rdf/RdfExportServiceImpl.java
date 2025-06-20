package org.folio.linked.data.service.rdf;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.folio.linked.data.domain.dto.RdfResourceDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RdfExportServiceImpl implements RdfExportService {

  private final Rdf4LdService rdf4LdService;
  private final ResourceRepository resourceRepository;
  private final ResourceModelMapper resourceModelMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public RdfResourceDto exportInstanceToRdf(Long id) {
    return resourceRepository.findById(id)
      .map(r -> {
        if (r.isOfType(INSTANCE)) {
          return r;
        } else {
          throw exceptionBuilder.notFoundLdResourceByIdException(INSTANCE.name(), String.valueOf(id));
        }
      })
      .map(resourceModelMapper::toModel)
      .map(r -> rdf4LdService.mapLdToBibframe2Rdf(r, RDFFormat.JSONLD))
      .map(ByteArrayOutputStream::toString)
      .map(rdf -> new RdfResourceDto().id(String.valueOf(id)).rdf(rdf))
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException(INSTANCE.name(), String.valueOf(id)));
  }

}
