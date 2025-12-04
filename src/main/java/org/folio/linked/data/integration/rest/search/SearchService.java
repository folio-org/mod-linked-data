package org.folio.linked.data.integration.rest.search;

import java.util.Collection;
import java.util.List;
import org.folio.linked.data.domain.dto.AuthorityItem;

public interface SearchService {

  Long countInstancesByLccnExcludingSuppressedAndId(Collection<String> lccns, String excludeId);

  List<AuthorityItem> getAuthoritiesByLccn(Collection<String> lccns);
}
