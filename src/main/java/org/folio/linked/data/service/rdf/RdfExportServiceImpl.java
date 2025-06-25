package org.folio.linked.data.service.rdf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RdfExportServiceImpl implements RdfExportService {

  private final Rdf4LdService rdf4LdService;
  private final ResourceRepository resourceRepository;
  private final ResourceModelMapper resourceModelMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public String exportInstanceToRdf(Long id) {
    return resourceRepository.findById(id)
      .filter(isInstance())
      .map(resourceModelMapper::toModel)
      .map(r -> rdf4LdService.mapLdToBibframe2Rdf(r, RDFFormat.JSONLD))
      .map(os -> os.toString(UTF_8))
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException(INSTANCE.name(), String.valueOf(id)));
  }

  private Predicate<Resource> isInstance() {
    return r -> {
      if (r.isOfType(INSTANCE)) {
        return true;
      } else {
        var message = "Resource with given id [" + r.getId() + "] is not an Instance";
        throw exceptionBuilder.badRequestException(message, "Not an Instance");
      }
    };
  }

}
