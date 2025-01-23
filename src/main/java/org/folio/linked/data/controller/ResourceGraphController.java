package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.rest.resource.GraphApi;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ResourceGraphController implements GraphApi {

  private final ResourceGraphService resourceGraphService;

  @Override
  public ResponseEntity<ResourceGraphDto> getResourceGraphById(Long id) {
    return ResponseEntity.ok(resourceGraphService.getResourceGraph(id));
  }
}
