package org.folio.linked.data.integration.rest.authoritysource;

import static org.folio.linked.data.util.Constants.Cache.AUTHORITY_SOURCE_FILES;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.linked.data.domain.dto.AuthoritySourceFiles;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Profile("!" + STANDALONE_PROFILE)
@HttpExchange("authority-source-files")
public interface AuthoritySourceFilesClient {

  @SuppressWarnings("java:S7180")
  @Cacheable(cacheNames = AUTHORITY_SOURCE_FILES, key = "@folioExecutionContext.tenantId + '_' + #limit")
  @GetExchange
  AuthoritySourceFiles getAuthoritySourceFiles(@RequestParam("limit") int limit);
}
