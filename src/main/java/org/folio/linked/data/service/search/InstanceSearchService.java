package org.folio.linked.data.service.search;

import java.util.Collection;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;

public interface InstanceSearchService {

  SearchResponseTotalOnly searchByLccn(Collection<String> lccn);

  SearchResponseTotalOnly searchByLccnExcludingId(Collection<String> lccn, String instanceId);
}
