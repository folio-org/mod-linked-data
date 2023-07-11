package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.noContent;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.rest.resource.ResourcesApi;
import org.folio.linked.data.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ResourceController implements ResourcesApi {

  private final ResourceService resourceService;

  @Override
  public ResponseEntity<BibframeResponse> createBibframe(String okapiTenant, BibframeRequest bibframeRequest) {
    return ResponseEntity.ok(resourceService.createBibframe(bibframeRequest));
  }

  @Override
  public ResponseEntity<BibframeResponse> getBibframeById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceService.getBibframeById(id));
  }

  @Override
  public ResponseEntity<BibframeResponse> updateBibframe(Long id, String okapiTenant, BibframeRequest bibframeRequest) {
    return ResponseEntity.ok(resourceService.updateBibframe(id, bibframeRequest));
  }

  @Override
  public ResponseEntity<Void> deleteBibframe(Long id, String okapiTenant) {
    resourceService.deleteBibframe(id);
    return noContent().build();
  }

  @Override
  public ResponseEntity<BibframeShortInfoPage> getBibframeShortInfoPage(String okapiTenant, Integer pageNumber,
                                                                         Integer pageSize) {
    return ResponseEntity.ok(resourceService.getBibframeShortInfoPage(pageNumber, pageSize));
  }

}
