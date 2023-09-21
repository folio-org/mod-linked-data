package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.noContent;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.rest.resource.ResourceApi;
import org.folio.linked.data.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ResourceController implements ResourceApi {

  private final ResourceService resourceService;

  @Override
  public ResponseEntity<ResourceDto> createResource(String okapiTenant, ResourceDto resourceDto) {
    return ResponseEntity.ok(resourceService.createResource(resourceDto));
  }

  @Override
  public ResponseEntity<ResourceDto> getResourceById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceService.getResourceById(id));
  }

  @Override
  public ResponseEntity<ResourceDto> updateResource(Long id, String okapiTenant,
                                                    ResourceDto resourceDto) {
    return ResponseEntity.ok(resourceService.updateResource(id, resourceDto));
  }

  @Override
  public ResponseEntity<Void> deleteResource(Long id, String okapiTenant) {
    resourceService.deleteResource(id);
    return noContent().build();
  }

  @Override
  public ResponseEntity<ResourceShortInfoPage> getResourceShortInfoPage(String type, String okapiTenant,
                                                                        Integer pageNumber, Integer pageSize) {
    return ResponseEntity.ok(resourceService.getResourceShortInfoPage(type, pageNumber, pageSize));
  }
}
