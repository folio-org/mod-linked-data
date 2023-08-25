package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.noContent;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.rest.resource.Bibframe2Api;
import org.folio.linked.data.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class Bibframe2Controller implements Bibframe2Api {

  private final ResourceService resourceService;

  @Override
  public ResponseEntity<Bibframe2Response> createBibframe2(String okapiTenant, Bibframe2Request bibframeRequest) {
    return ResponseEntity.ok(resourceService.createBibframe2(bibframeRequest));
  }

  @Override
  public ResponseEntity<Bibframe2Response> getBibframe2ById(Long id, String okapiTenant) {
    return ResponseEntity.ok(resourceService.getBibframe2ById(id));
  }

  @Override
  public ResponseEntity<Bibframe2Response> updateBibframe2(Long id, String okapiTenant,
                                                           Bibframe2Request bibframeRequest) {
    return ResponseEntity.ok(resourceService.updateBibframe2(id, bibframeRequest));
  }

  @Override
  public ResponseEntity<Void> deleteBibframe2(Long id, String okapiTenant) {
    resourceService.deleteBibframe2(id);
    return noContent().build();
  }

  @Override
  public ResponseEntity<BibframeShortInfoPage> getBibframe2ShortInfoPage(String okapiTenant, Integer pageNumber,
                                                                         Integer pageSize) {
    return ResponseEntity.ok(resourceService.getBibframe2ShortInfoPage(pageNumber, pageSize));
  }

}
