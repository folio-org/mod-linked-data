package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IndexRequest;
import org.folio.linked.data.domain.dto.IndexResponse;
import org.folio.linked.data.rest.resource.IndexApi;
import org.folio.linked.data.service.IndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class IndexingController implements IndexApi {

  private final IndexService indexService;

  @Override
  public ResponseEntity<IndexResponse> createIndex(String okapiTenant, IndexRequest request) {
    return ResponseEntity.ok(indexService.creaetIndex(request));
  }
}
