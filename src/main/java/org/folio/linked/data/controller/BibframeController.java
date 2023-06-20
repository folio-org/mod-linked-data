package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.rest.resource.BibframesApi;
import org.folio.linked.data.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class BibframeController implements BibframesApi {

  private final ResourceService resourceService;

  @Override
  public ResponseEntity<BibframeResponse> getBibframeById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceService.getBibframeById(id));
  }

  @Override
  public ResponseEntity<BibframeShortInfoPage> getBibframesShortInfoPage(String okapiTenant, Integer pageNumber,
                                                                         Integer pageSize) {
    return ResponseEntity.ok(resourceService.getBibframeShortInfoPage(pageNumber, pageSize));
  }

}
