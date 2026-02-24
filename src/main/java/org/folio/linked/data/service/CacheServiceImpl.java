package org.folio.linked.data.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

  private final CacheManager cacheManager;

  @Override
  public void clearCaches() {
    log.info("Emptying all caches");
    cacheManager.getCacheNames()
      .stream()
      .map(cacheManager::getCache)
      .filter(Objects::nonNull)
      .forEach(Cache::clear);
  }
}
