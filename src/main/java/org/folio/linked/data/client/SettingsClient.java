package org.folio.linked.data.client;

import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;

import org.folio.linked.data.domain.dto.SettingsSearchResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("settings")
public interface SettingsClient {

  @Cacheable(cacheNames = SETTINGS_ENTRIES, key = "#query")
  @GetMapping("/entries")
  ResponseEntity<SettingsSearchResponse> getEntries(@RequestParam("query") String query);
}
