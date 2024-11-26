package org.folio.linked.data.service.search;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.client.SearchClient;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstanceSearchServiceImpl implements InstanceSearchService {

  private static final String EXCLUDE_SUPPRESSED = "staffSuppress <> \"true\" and discoverySuppress <> \"true\"";
  private static final String ID_NOT_EQUALS = "id <> \"%s\"";
  private static final String LCCN_EQUALS = "lccn==\"%s\"";

  private final SearchClient searchClient;

  @Override
  public SearchResponseTotalOnly searchByLccn(Collection<String> lccn) {
    return search(getLccnQuery(lccn));
  }

  @Override
  public SearchResponseTotalOnly searchByLccnExcludingId(Collection<String> lccn, String instanceId) {
    if (isNull(instanceId)) {
      return searchByLccn(lccn);
    }
    return search(format("%s and %s", getLccnQuery(lccn), format(ID_NOT_EQUALS, instanceId)));
  }

  private SearchResponseTotalOnly search(String query) {
    return searchClient.searchInstances(query).getBody();
  }

  private String getLccnQuery(Collection<String> lccnCol) {
    var orLccnQuery = lccnCol.stream()
      .filter(Objects::nonNull)
      .map(lccn -> format(LCCN_EQUALS, lccn))
      .collect(joining(" or "));
    return format("(%s) and (%s)", orLccnQuery, EXCLUDE_SUPPRESSED);
  }
}
