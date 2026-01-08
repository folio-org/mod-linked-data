package org.folio.linked.data.integration.rest.authoritysource;

import static org.folio.linked.data.util.Constants.Cache.AUTHORITY_SOURCE_FILES;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.linked.data.domain.dto.AuthoritySourceFiles;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "authority-source-files")
@Profile("!" + STANDALONE_PROFILE)
public interface AuthoritySourceFilesClient {

  @SuppressWarnings("java:S7180")
  @Cacheable(cacheNames = AUTHORITY_SOURCE_FILES, key = "@folioExecutionContext.tenantId + '_' + #limit")
  @GetMapping
  AuthoritySourceFiles getAuthoritySourceFiles(@RequestParam("limit") int limit);
}
