package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.AdminApi;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

  private final CacheManager cacheManager;

  @Override
  public ResponseEntity<Void> clearCaches() {
    cacheManager.getCacheNames()
      .stream()
      .map(cacheManager::getCache)
      .forEach(this::clearCache);
    return ResponseEntity.noContent().build();
  }

  private void clearCache(Cache cache) {
    if (cache != null) {
      cache.clear();
    }
  }
}
