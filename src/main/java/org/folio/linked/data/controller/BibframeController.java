package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.noContent;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.rest.resource.BibframesApi;
import org.folio.linked.data.service.BibframeService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class BibframeController implements BibframesApi {

  private final BibframeService bibframeService;

  @Override
  public ResponseEntity<BibframeResponse> createBibframe(String okapiTenant,
                                                         BibframeCreateRequest bibframeCreateRequest) {
    return ResponseEntity.ok(bibframeService.createBibframe(okapiTenant, bibframeCreateRequest));
  }

  @Override
  public ResponseEntity<BibframeResponse> getBibframeById(String okapiTenant, Long id) {
    return ResponseEntity.ok(bibframeService.getBibframeById(okapiTenant, id));
  }

  @Override
  public ResponseEntity<BibframeResponse> updateBibframe(String okapiTenant, Long id,
                                                         BibframeUpdateRequest bibframeUpdateRequest) {
    return ResponseEntity.ok(bibframeService.updateBibframe(okapiTenant, id, bibframeUpdateRequest));
  }

  @Override
  public ResponseEntity<Void> deleteBibframe(String okapiTenant, Long id) {
    bibframeService.deleteBibframe(okapiTenant, id);
    return noContent().build();
  }

  @Override
  public ResponseEntity<BibframeShortInfoPage> getBibframesShortInfoPage(String okapiTenant, Integer pageNumber,
                                                                         Integer pageSize) {
    return ResponseEntity.ok(bibframeService.getBibframeShortInfoPage(okapiTenant, pageNumber, pageSize));
  }
}
