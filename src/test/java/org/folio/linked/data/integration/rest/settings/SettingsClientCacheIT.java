package org.folio.linked.data.integration.rest.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.executeAsyncWithContext;
import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

@IntegrationTest
class SettingsClientCacheIT {

  @Autowired
  private SettingsClient settingsClient;

  @Autowired
  private ExecutionContextBuilder contextBuilder;

  @Autowired
  private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    clearCache();
  }

  @AfterEach
  void tearDown() {
    clearCache();
  }

  @Test
  void shouldNotShareCacheAcrossTenants() {
    // given
    var tenant1 = TENANT_ID;
    var query = "key==test.setting";

    // when - populate cache for tenant1
    executeAsyncWithContext(contextBuilder, tenant1, () -> settingsClient.getEntries(query));

    // Verify tenant1 has cache entry
    var cache = cacheManager.getCache(SETTINGS_ENTRIES);
    assertThat(cache).isNotNull();
    var tenant1CacheKey = tenant1 + "_" + query;
    assertThat(cache.get(tenant1CacheKey)).isNotNull();

    // when - check tenant2 cache before any call
    var tenant2 = "another_tenant";
    var tenant2CacheKey = tenant2 + "_" + query;
    var tenant2CacheValue = cache.get(tenant2CacheKey);

    // then - tenant2 should not have any cached value yet
    assertThat(tenant2CacheValue).isNull();

    // when - make call for tenant2
    executeAsyncWithContext(contextBuilder, tenant2, () -> settingsClient.getEntries(query));

    // then - now tenant2 should have its own cache entry
    assertThat(cache.get(tenant2CacheKey)).isNotNull();

    // Verify both tenants have separate cache entries with different keys
    assertThat(cache.get(tenant1CacheKey)).isNotNull();
    assertThat(cache.get(tenant2CacheKey)).isNotNull();

    // Verify that the cache keys are indeed different
    assertThat(tenant1CacheKey).isNotEqualTo(tenant2CacheKey);
  }

  private void clearCache() {
    var cache = cacheManager.getCache(SETTINGS_ENTRIES);
    if (cache != null) {
      cache.clear();
    }
  }
}

