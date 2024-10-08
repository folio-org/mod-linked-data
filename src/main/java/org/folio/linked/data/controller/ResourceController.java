package org.folio.linked.data.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.rest.resource.ResourceApi;
import org.folio.linked.data.service.resource.ResourceMarcService;
import org.folio.linked.data.service.resource.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceController implements ResourceApi {

  private final ResourceService resourceService;
  private final ResourceMarcService resourceMarcService;

  @Override
  public ResponseEntity<ResourceResponseDto> createResource(String okapiTenant, @Valid ResourceRequestDto resourceDto) {
    return ResponseEntity.ok(resourceService.createResource(resourceDto));
  }

  @Override
  public ResponseEntity<ResourceResponseDto> getResourceById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceService.getResourceById(id));
  }

  @Override
  public ResponseEntity<ResourceIdDto> getResourceIdByResourceInventoryId(String inventoryId, String okapiTenant) {
    return ResponseEntity.ok(resourceService.getResourceIdByInventoryId(inventoryId));
  }

  @Override
  public ResponseEntity<String> isSupportedByInventoryId(String inventoryId) {
    return ResponseEntity.ok(resourceMarcService.isSupportedByInventoryId(inventoryId).toString());
  }

  @Override
  public ResponseEntity<ResourceResponseDto> updateResource(Long id, String okapiTenant,
                                                    @Valid ResourceRequestDto resourceDto) {
    return ResponseEntity.ok(resourceService.updateResource(id, resourceDto));
  }

  @Override
  public ResponseEntity<Void> deleteResource(Long id, String okapiTenant) {
    resourceService.deleteResource(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<ResourceMarcViewDto> getResourceMarcViewById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceMarcService.getResourceMarcView(id));
  }
}
