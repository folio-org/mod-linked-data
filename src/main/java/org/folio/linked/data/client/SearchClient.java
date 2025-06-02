package org.folio.linked.data.client;

import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "search")
public interface SearchClient {

  @GetMapping("/instances")
  ResponseEntity<SearchResponseTotalOnly> searchInstances(@RequestParam("query") String query);

  @GetMapping("/authorities")
  ResponseEntity<AuthoritySearchResponse> searchAuthorities(@RequestParam("query") String query);
}
