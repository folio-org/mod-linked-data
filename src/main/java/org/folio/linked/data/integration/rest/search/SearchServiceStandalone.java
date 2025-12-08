package org.folio.linked.data.integration.rest.search;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Collection;
import java.util.List;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(STANDALONE_PROFILE)
public class SearchServiceStandalone implements SearchService {

  @Override
  public Long countInstancesByLccnExcludingSuppressedAndId(Collection<String> lccns, String excludeId) {
    return 0L;
  }

  @Override
  public List<AuthorityItem> getAuthoritiesByLccn(Collection<String> lccns) {
    return List.of();
  }

}
