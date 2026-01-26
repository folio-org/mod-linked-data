package org.folio.linked.data.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.executeAsyncWithContext;
import static org.folio.linked.data.test.TestUtil.executeWithContext;
import static org.folio.linked.data.util.Constants.Cache.PROFILES;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

@IntegrationTest
class ProfileServiceCacheIT {

  private static final int TEST_PROFILE_ID = 999;

  @Autowired
  private ProfileService profileService;

  @Autowired
  private ProfileRepository profileRepository;

  @Autowired
  private ResourceTypeRepository resourceTypeRepository;

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
    cleanupTestProfiles();
  }

  @Test
  void shouldNotShareCacheAcrossTenants() {
    // given
    var tenant1 = TENANT_ID;
    var profileId = TEST_PROFILE_ID;
    executeAsyncWithContext(contextBuilder, tenant1, () -> createTestProfile(profileId, "Tenant1 Profile"));

    // Clear cache to ensure fresh start
    clearCache();

    // when - populate cache for tenant1
    var profile1 = executeWithContext(contextBuilder, tenant1, () -> profileService.getProfileById(profileId));

    // Verify tenant1 has cache entry
    var cache = cacheManager.getCache(PROFILES);
    assertThat(cache).isNotNull();
    var tenant1CacheKey = tenant1 + "_" + profileId;
    assertThat(cache.get(tenant1CacheKey)).isNotNull();

    // Verify profile was loaded for tenant1 from tenant1's schema
    assertThat(profile1).isNotNull();
    assertThat(profile1.getId()).isEqualTo(profileId);
    assertThat(profile1.getName()).isEqualTo("Tenant1 Profile");

    // when - check tenant2 cache before any call
    var tenant2 = "another_tenant";
    var tenant2CacheKey = tenant2 + "_" + profileId;
    var tenant2CacheValue = cache.get(tenant2CacheKey);

    // then - tenant2 should not have any cached value yet
    assertThat(tenant2CacheValue).isNull();
    // Create profile for tenant2
    executeAsyncWithContext(contextBuilder, tenant2, () -> createTestProfile(profileId, "Tenant2 Profile"));

    // when - make call for tenant2
    var profile2 = executeWithContext(contextBuilder, tenant2, () -> profileService.getProfileById(profileId));

    // then - now tenant2 should have its own cache entry
    assertThat(cache.get(tenant2CacheKey)).isNotNull();

    // Verify profiles have SAME ID but different names
    assertThat(profile1.getId()).isEqualTo(profile2.getId());
    assertThat(profile1.getName()).isNotEqualTo(profile2.getName());

    // Verify both tenants have separate cache entries with different keys
    assertThat(cache.get(tenant1CacheKey)).isNotNull();
    assertThat(cache.get(tenant2CacheKey)).isNotNull();
  }

  private void createTestProfile(int id, String name) {
    // Get existing resource type (Work type exists in test data)
    var resourceType = resourceTypeRepository.findByUri("http://bibfra.me/vocab/lite/Work");
    if (resourceType == null) {
      throw new IllegalStateException("Work resource type not found");
    }

    var profile = new Profile()
      .setId(id)
      .setName(name)
      .setResourceType(resourceType)
      .setValue("{\"profile\": \"" + name + "\"}");

    profileRepository.save(profile);
  }

  private void cleanupTestProfiles() {
    // Delete profile with same ID from both tenant schemas
    executeAsyncWithContext(contextBuilder, TENANT_ID, () -> profileRepository.deleteById(TEST_PROFILE_ID));
    executeAsyncWithContext(contextBuilder, "another_tenant", () -> profileRepository.deleteById(TEST_PROFILE_ID));
  }

  private void clearCache() {
    var cache = cacheManager.getCache(PROFILES);
    if (cache != null) {
      cache.clear();
    }
  }
}
