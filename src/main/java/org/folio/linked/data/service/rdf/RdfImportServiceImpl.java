package org.folio.linked.data.service.rdf;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Component
@Transactional
@RequiredArgsConstructor
public class RdfImportServiceImpl implements RdfImportService {

  private final Rdf4LdService rdf4LdService;
  private final MetadataService metadataService;
  private final ResourceRepository resourceRepo;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public List<Long> importFile(MultipartFile multipartFile) {
    try (var is = multipartFile.getInputStream()) {
      var resources = rdf4LdService.mapToLdInstance(is, multipartFile.getContentType());
      return save(resources);
    } catch (IOException e) {
      throw exceptionBuilder.badRequestException("Rdf import incoming file reading error", e.getMessage());
    }
  }

  private List<Long> save(Set<org.folio.ld.dictionary.model.Resource> resources) {
    return resources.stream()
      .map(resourceModelMapper::toEntity)
      .filter(r -> {
        boolean exists = resourceRepo.existsById(r.getId());
        if (exists) {
          log.warn("Instance with id {} was ignored during RDF import because it exists already", r.getId());
        }
        return !exists;
      })
      .map(resource -> {
        metadataService.ensure(resource);
        var saved = resourceGraphService.saveMergingGraph(resource);
        applicationEventPublisher.publishEvent(new ResourceCreatedEvent(saved));
        return resource.getId();
      })
      .toList();
  }
}
