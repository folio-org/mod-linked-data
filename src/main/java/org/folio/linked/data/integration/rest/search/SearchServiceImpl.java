package org.folio.linked.data.integration.rest.search;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.linked.data.util.SearchQueryUtils.AND;
import static org.folio.linked.data.util.SearchQueryUtils.queryIdNotEquals;
import static org.folio.linked.data.util.SearchQueryUtils.queryLccnsAuthorized;
import static org.folio.linked.data.util.SearchQueryUtils.queryLccnsExcludingSuppressed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
  private final SearchClient searchClient;

  @Value("${mod-linked-data.search.max-params}")
  private int searchMaxParams;

  @Override
  public Long countInstancesByLccnExcludingSuppressedAndId(Collection<String> lccns,
                                                           String excludeId) {
    return partitionAndSearch(lccns, chunk -> {
      var query = queryLccnsExcludingSuppressed(chunk);
      if (!isNull(excludeId)) {
        query = query + AND + queryIdNotEquals(excludeId);
      }
      return searchClient.searchInstances(query).getBody().getTotalRecords();
    }).stream()
      .mapToLong(Long::longValue)
      .sum();
  }

  @Override
  public List<AuthorityItem> getAuthoritiesByLccn(Collection<String> lccns) {
    return partitionAndSearch(lccns, chunk -> {
      var searchQuery = queryLccnsAuthorized(chunk);
      return ofNullable(searchClient.searchAuthorities(searchQuery).getBody())
        .stream()
        .flatMap(body -> body.getAuthorities().stream())
        .toList();
    }).stream()
      .flatMap(List::stream)
      .toList();
  }

  private <T> List<T> partitionAndSearch(Collection<String> items,
                                         Function<List<String>, T> processor) {
    return ListUtils.partition(new ArrayList<>(items), searchMaxParams).stream()
      .map(processor)
      .toList();
  }

}
