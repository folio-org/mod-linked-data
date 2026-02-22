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

    var specRulesCache = cacheManager.getCache(SPEC_RULES);
    assertThat(specRulesCache).isNotNull();
    var tenant1RuleKey = "tenant1-rule";
    specRulesCache.put(tenant1RuleKey, "rule-value");
    assertThat(specRulesCache.get(tenant1RuleKey)).isNotNull();

    var profilesCache = cacheManager.getCache(PROFILES);
    assertThat(profilesCache).isNotNull();
    var tenant1ProfileKey = "tenant1-profile";
    profilesCache.put(tenant1ProfileKey, "profile-value");
    assertThat(profilesCache.get(tenant1ProfileKey)).isNotNull();

    var cacheService = new CacheServiceImpl(cacheManager);

    // when
    cacheService.clearCaches();

    // then
    assertThat(specRulesCache.get(tenant1RuleKey)).isNull();
    assertThat(profilesCache.get(tenant1ProfileKey)).isNull();
  }
}
