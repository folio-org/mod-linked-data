package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.ReindexApi;
import org.folio.linked.data.service.ReindexService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ReindexingController implements ReindexApi {

  private final ReindexService reIndexService;

  @Override
  public ResponseEntity<Void> reindex(String okapiTenant) {
    reIndexService.reindex();
    return ResponseEntity.noContent().build();
  }
}
