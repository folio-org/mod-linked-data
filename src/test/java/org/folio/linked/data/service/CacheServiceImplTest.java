package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.util.Constants.Cache.PROFILES;
import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

@UnitTest
class CacheServiceImplTest {

  @Test
  void clearCaches_shouldClearAllCaches() {
    // given
    var cacheManager = new CaffeineCacheManager();

    var specRulesCache = cacheManager.getCache("spec-rules");
    assertThat(specRulesCache).isNotNull();
    specRulesCache.put(SPEC_RULES, "rule-value");
    assertThat(specRulesCache.get(SPEC_RULES)).isNotNull();

    var profilesCache = cacheManager.getCache("profiles");
    assertThat(profilesCache).isNotNull();
    profilesCache.put(PROFILES, "profile-value");
    assertThat(profilesCache.get(PROFILES)).isNotNull();

    var cacheService = new CacheServiceImpl(cacheManager);

    // when
    cacheService.clearCaches();

    // then
    assertThat(specRulesCache.get(SPEC_RULES)).isNull();
    assertThat(profilesCache.get(PROFILES)).isNull();
  }
}
