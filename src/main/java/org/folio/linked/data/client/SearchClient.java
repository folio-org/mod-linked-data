package org.folio.linked.data.client;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.folio.search.domain.dto.CreateIndexRequest;
import org.folio.search.domain.dto.CreateIndexResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "search")
@Profile(FOLIO_PROFILE)
public interface SearchClient {

  @PostMapping(value = "/index/indices", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<CreateIndexResponse> createIndex(@RequestBody CreateIndexRequest createIndexRequest);

}
