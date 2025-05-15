package org.folio.linked.data.service.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
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
  public Set<Long> importFile(MultipartFile multipartFile) {
    var is = getInputStream(multipartFile);
    var resources = rdf4LdService.mapToLdInstance(is, multipartFile.getContentType());
    return save(resources);
  }

  private InputStream getInputStream(MultipartFile multipartFile) {
    InputStream is;
    try {
      is = multipartFile.getInputStream();
    } catch (IOException e) {
      throw exceptionBuilder.badRequestException("Rdf import incoming file reading error", e.getMessage());
    }
    return is;
  }

  private Set<Long> save(Set<org.folio.ld.dictionary.model.Resource> resources) {
    return resources.stream()
      .map(resourceModelMapper::toEntity)
      .map(resource -> {
        var existing = resourceRepo.findById(resource.getId());
        if (existing.isPresent()) {
          resource.setFolioMetadata(existing.get().getFolioMetadata());
          var saved = resourceGraphService.saveMergingGraph(resource);
          applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(saved));
        } else {
          metadataService.ensure(resource);
          var saved = resourceGraphService.saveMergingGraph(resource);
          applicationEventPublisher.publishEvent(new ResourceCreatedEvent(saved));
        }
        return resource.getId();
      })
      .collect(Collectors.toSet());
  }
}
