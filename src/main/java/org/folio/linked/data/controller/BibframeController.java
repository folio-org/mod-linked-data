package org.folio.linked.data.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
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
  public ResponseEntity<BibframeResponse> getBibframeById(UUID uuid) {
    return ResponseEntity.ok(bibframeService.getBibframeBySlug(uuid.toString()));
  }
}
