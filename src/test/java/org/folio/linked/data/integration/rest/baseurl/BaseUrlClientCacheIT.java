package org.folio.linked.data.integration.rest.baseurl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.rest.settings.BaseUrlClient;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

@IntegrationTest
class BaseUrlClientCacheIT {

  @Autowired
  private BaseUrlClient baseUrlClient;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;

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

    // when - populate cache for tenant1
    tenantScopedExecutionService.execute(tenant1, () -> baseUrlClient.getBaseUrl());

    // Verify tenant1 has cache entry
    var cache = cacheManager.getCache(SETTINGS_ENTRIES);
    assertThat(cache).isNotNull();
    var tenant1CacheKey = tenant1 + "_base-url";
    assertThat(cache.get(tenant1CacheKey)).isNotNull();

    // when - check tenant2 cache before any call
    var tenant2 = "another_tenant";
    var tenant2CacheKey = tenant2 + "_base-url";
    var tenant2CacheValue = cache.get(tenant2CacheKey);

    // then - tenant2 should not have any cached value yet
    assertThat(tenant2CacheValue).isNull();

    // when - make call for tenant2
    tenantScopedExecutionService.execute(tenant2, () -> baseUrlClient.getBaseUrl());

    // then - now tenant2 should have its own cache entry
    assertThat(cache.get(tenant2CacheKey)).isNotNull();

    // Verify both tenants have separate cache entries
    assertThat(cache.get(tenant1CacheKey).get()).isNotNull();
    assertThat(cache.get(tenant2CacheKey).get()).isNotNull();
    assertThat(cache.get(tenant1CacheKey).get()).isNotEqualTo(cache.get(tenant2CacheKey).get());
  }

  private void clearCache() {
    var cache = cacheManager.getCache(SETTINGS_ENTRIES);
    if (cache != null) {
      cache.clear();
    }
  }
}
