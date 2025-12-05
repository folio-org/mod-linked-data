package org.folio.linked.data.integration.rest.search;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "search")
@Profile("!" + STANDALONE_PROFILE)
public interface SearchClient {

  @GetMapping("/instances")
  ResponseEntity<SearchResponseTotalOnly> searchInstances(@RequestParam("query") String query);

  @GetMapping("/authorities")
  ResponseEntity<AuthoritySearchResponse> searchAuthorities(@RequestParam("query") String query);
}
