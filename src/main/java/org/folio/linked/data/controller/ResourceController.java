package org.folio.linked.data.controller;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceSubgraphViewDto;
import org.folio.linked.data.domain.dto.SearchResourcesRequestDto;
import org.folio.linked.data.rest.resource.ResourceApi;
import org.folio.linked.data.service.rdf.RdfExportService;
import org.folio.linked.data.service.resource.ResourceService;
import org.folio.linked.data.service.resource.marc.ResourceMarcBibService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResourceController implements ResourceApi {

  private final ResourceService resourceService;
  private final RdfExportService rdfExportService;
  private final ResourceMarcBibService resourceMarcService;

  @Override
  public ResponseEntity<ResourceResponseDto> createResource(@Valid ResourceRequestDto resourceDto) {
    return ResponseEntity.ok(resourceService.createResource(resourceDto));
  }

  @Override
  public ResponseEntity<ResourceResponseDto> getResourceById(Long id) {
    return ResponseEntity.ok(resourceService.getResourceById(id));
  }

  @Override
  public ResponseEntity<ResourceIdDto> getResourceIdByResourceInventoryId(String inventoryId) {
    return ResponseEntity.ok(resourceService.getResourceIdByInventoryId(inventoryId));
  }

  @Override
  public ResponseEntity<String> checkMarcBibImportableToGraph(String inventoryId) {
    return ResponseEntity.ok(Boolean.toString(resourceMarcService.checkMarcBibImportableToGraph(inventoryId)));
  }

  @Override
  public ResponseEntity<ResourceResponseDto> getResourcePreviewByInventoryId(String inventoryId) {
    return ResponseEntity.ok(resourceMarcService.getResourcePreviewByInventoryId(inventoryId));
  }

  @Override
  public ResponseEntity<ResourceIdDto> importMarcRecord(String inventoryId, Integer profileId) {
    return ResponseEntity
      .status(CREATED)
      .body(resourceMarcService.importMarcRecord(inventoryId, profileId));
  }

  @Override
  public ResponseEntity<ResourceResponseDto> updateResource(@NotNull Long id,
                                                            @Nullable @Valid ResourceRequestDto resourceDto) {
    return ResponseEntity.ok(resourceService.updateResource(id, resourceDto));
  }

  @Override
  public ResponseEntity<Void> deleteResource(Long id) {
    resourceService.deleteResource(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<ResourceMarcViewDto> getResourceMarcViewById(Long id) {
    return ResponseEntity.ok(resourceMarcService.getResourceMarcView(id));
  }

  @Override
  public ResponseEntity<String> exportInstanceToRdf(Long id) {
    return ResponseEntity.ok(rdfExportService.exportInstanceToRdf(id));
  }

  @Override
  public ResponseEntity<Set<ResourceSubgraphViewDto>> searchResources(SearchResourcesRequestDto searchRequest) {
    return ResponseEntity.ok(resourceService.searchResources(searchRequest));
  }

  @InitBinder
  public void bindIdForResourcePutRequest(WebDataBinder binder, HttpServletRequest request) {
    var target = binder.getTarget();
    if (target instanceof ResourceRequestDto dto && isPutRequest(request)) {
      dto.setId(idFromUri(request.getRequestURI()));
    }
  }

  private boolean isPutRequest(HttpServletRequest request) {
    return ofNullable(request)
      .map(r -> PUT.name().equalsIgnoreCase(r.getMethod()))
      .orElse(false);
  }

  private Long idFromUri(String path) {
    String[] parts = path.split("/");
    return Long.parseLong(parts[parts.length - 1]);
  }
}
