package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.noContent;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
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
  public ResponseEntity<BibframeResponse> getBibframeBySlug(String okapiTenant, String slug) {
    return ResponseEntity.ok(bibframeService.getBibframeBySlug(okapiTenant, slug));
  }

  @Override
  public ResponseEntity<BibframeResponse> updateBibframe(String okapiTenant, String slug,
                                                         BibframeUpdateRequest bibframeUpdateRequest) {
    return ResponseEntity.ok(bibframeService.updateBibframe(okapiTenant, slug, bibframeUpdateRequest));
  }

  @Override
  public ResponseEntity<Void> deleteBibframe(String okapiTenant, String slug) {
    bibframeService.deleteBibframe(okapiTenant, slug);
    return noContent().build();
  }
}
