package org.folio.linked.data.client;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.folio.search.domain.dto.CreateIndexRequest;
import org.folio.search.domain.dto.FolioCreateIndexResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "search", url = "http://localhost:8085")
@Profile(SEARCH_PROFILE)
public interface SearchClient {

  @PostMapping(value = "/search/index/indices", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<FolioCreateIndexResponse> createIndex(@RequestBody CreateIndexRequest createIndexRequest);

}
