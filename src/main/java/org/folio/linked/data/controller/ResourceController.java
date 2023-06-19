package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceResponse;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.rest.resource.ResourcesApi;
import org.folio.linked.data.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ResourceController implements ResourcesApi {

  private final ResourceService resourceSerivce;

  @Override
  public ResponseEntity<ResourceResponse> getResourceById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceSerivce.getResourceById(id));
  }

  @Override
  public ResponseEntity<ResourceShortInfoPage> getResourcesShortInfoPage(String okapiTenant, Integer pageNumber,
                                                                         Integer pageSize) {
    return ResponseEntity.ok(resourceSerivce.getResourceShortInfoPage(pageNumber, pageSize));
  }

}
