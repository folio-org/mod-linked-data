package org.folio.linked.data.configuration;

import java.util.Collections;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

  public static final String SPEC_RULES = "specRules";

  @Bean
  public CacheManager cacheManager() {
    var cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Collections.singletonList(new ConcurrentMapCache(SPEC_RULES)));
    return cacheManager;
  }
}
