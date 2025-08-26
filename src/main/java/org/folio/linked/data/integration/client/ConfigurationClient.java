package org.folio.linked.data.integration.client;

import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;

import org.folio.linked.data.domain.dto.Configurations;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "configurations")
public interface ConfigurationClient {

  @SuppressWarnings("java:S7180")
  @Cacheable(cacheNames = SETTINGS_ENTRIES, key = "#moduleName")
  @GetMapping("${config.client.path:/entries}?query=module=={moduleName}")
  Configurations lookupConfigByModuleName(@PathVariable("moduleName") String moduleName);

}
