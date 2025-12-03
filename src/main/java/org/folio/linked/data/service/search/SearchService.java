package org.folio.linked.data.service.search;

import java.util.Collection;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;

public interface SearchService {

  SearchResponseTotalOnly getTotalInstancesByLccnExcludingSuppressedAndId(Collection<String> lccns, String excludeId);

  AuthoritySearchResponse getAuthoritiesByLccn(Collection<String> lccns);
}
