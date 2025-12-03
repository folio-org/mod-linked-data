package org.folio.linked.data.service.search;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.SearchQueryUtils.AND;
import static org.folio.linked.data.util.SearchQueryUtils.queryIdNotEquals;
import static org.folio.linked.data.util.SearchQueryUtils.queryLccns;
import static org.folio.linked.data.util.SearchQueryUtils.queryLccnsExcludingSuppressed;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.folio.linked.data.integration.client.SearchClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
  private final SearchClient searchClient;

  @Override
  public SearchResponseTotalOnly getTotalInstancesByLccnExcludingSuppressedAndId(Collection<String> lccns,
                                                                                 String excludeId) {
    if (isNull(excludeId)) {
      return searchInstances(queryLccnsExcludingSuppressed(lccns));
    }
    return searchInstances(queryLccnsExcludingSuppressed(lccns) + AND + queryIdNotEquals(excludeId));
  }

  @Override
  public AuthoritySearchResponse getAuthoritiesByLccn(Collection<String> lccns) {
    var searchQuery = queryLccns(lccns);
    return searchClient.searchAuthorities(searchQuery).getBody();
  }

  private SearchResponseTotalOnly searchInstances(String query) {
    return searchClient.searchInstances(query).getBody();
  }

}
