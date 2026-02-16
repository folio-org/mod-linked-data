package org.folio.linked.data.integration.rest.configuration;

import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.linked.data.domain.dto.Configurations;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("configurations")
@Profile("!" + STANDALONE_PROFILE)
public interface ConfigurationClient {

  @SuppressWarnings("java:S7180")
  @Cacheable(cacheNames = SETTINGS_ENTRIES, key = "@folioExecutionContext.tenantId + '_' + #code")
  @GetExchange("/entries?query=module=={moduleName} and code=={code}")
  Configurations lookupConfig(@PathVariable("moduleName") String moduleName,
                              @PathVariable("code") String code);

}
