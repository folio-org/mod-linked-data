package org.folio.linked.data.controller;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IndexRequest;
import org.folio.linked.data.domain.dto.IndexResponse;
import org.folio.linked.data.rest.resource.ReindexApi;
import org.folio.linked.data.service.IndexService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class IndexingController implements ReindexApi {

  private final IndexService indexService;

  @Override
  public ResponseEntity<IndexResponse> createIndex(String okapiTenant, IndexRequest request) {
    return ResponseEntity.ok(indexService.createIndex(request));
  }
}
