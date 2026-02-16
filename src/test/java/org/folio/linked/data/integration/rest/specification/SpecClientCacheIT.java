package org.folio.linked.data.integration.rest.specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;

import java.util.UUID;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

@IntegrationTest
class SpecClientCacheIT {

  @Autowired
  private SpecClient specClient;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;

  @BeforeEach
  void setUp() {
    clearCaches();
  }

  @AfterEach
  void tearDown() {
    clearCaches();
  }

  @Test
  void shouldNotShareCacheAcrossTenants() {
    // given
    var tenant1 = TENANT_ID;
    var specId = UUID.fromString("bbb1f951-1f30-52e5-b827-557766551002");

    // when - populate cache for tenant1
    tenantScopedExecutionService.execute(tenant1, () -> specClient.getSpecRules(specId));

    // Verify tenant1 has cache entry
    var cache = cacheManager.getCache(SPEC_RULES);
    assertThat(cache).isNotNull();
    var tenant1CacheKey = tenant1 + "_" + specId;
    assertThat(cache.get(tenant1CacheKey)).isNotNull();

    // when - check tenant2 cache before any call
    var tenant2 = "another_tenant";
    var tenant2CacheKey = tenant2 + "_" + specId;
    var tenant2CacheValue = cache.get(tenant2CacheKey);

    // then - tenant2 should not have any cached value yet
    assertThat(tenant2CacheValue).isNull();

    // when - make call for tenant2
    tenantScopedExecutionService.execute(tenant2, () -> specClient.getSpecRules(specId));

    // then - now tenant2 should have its own cache entry
    assertThat(cache.get(tenant2CacheKey)).isNotNull();

    // Verify both tenants have separate cache entries with different keys
    assertThat(cache.get(tenant1CacheKey)).isNotNull();
    assertThat(cache.get(tenant2CacheKey)).isNotNull();

    // Verify that the cache keys are indeed different
    assertThat(tenant1CacheKey).isNotEqualTo(tenant2CacheKey);
  }

  private void clearCaches() {
    var bibMarcSpecsCache = cacheManager.getCache("bib-marc-specs");
    if (bibMarcSpecsCache != null) {
      bibMarcSpecsCache.clear();
    }
    var specRulesCache = cacheManager.getCache(SPEC_RULES);
    if (specRulesCache != null) {
      specRulesCache.clear();
    }
  }
}

