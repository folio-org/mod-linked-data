package org.folio.linked.data.integration.rest.search;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Profile("!" + STANDALONE_PROFILE)
@HttpExchange("search")
public interface SearchClient {

  @GetExchange("/instances")
  ResponseEntity<SearchResponseTotalOnly> searchInstances(@RequestParam("query") String query);

  @GetExchange("/authorities")
  ResponseEntity<AuthoritySearchResponse> searchAuthorities(@RequestParam("query") String query);
}
