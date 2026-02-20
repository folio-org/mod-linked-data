package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.rest.resource.AdminApi.PATH_CLEAR_CACHES;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@UnitTest
class AdminControllerTest {

  @Test
  void clearCaches_shouldClearAllCachesAndReturnNoContent() throws Exception {
    // given
    var cacheManager = new CaffeineCacheManager();
    var ruleCacheKey = "tenant1_rule";
    var profileCacheKey = "tenant1_profile";

    var specRulesCache = cacheManager.getCache("spec-rules");
    specRulesCache.put(ruleCacheKey, "rule-value");
    assertThat(specRulesCache.get(ruleCacheKey)).isNotNull();

    var profilesCache = cacheManager.getCache("profiles");
    profilesCache.put(profileCacheKey, "profile-value");
    assertThat(profilesCache.get(profileCacheKey)).isNotNull();

    var mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(cacheManager)).build();

    // when
    mockMvc.perform(delete(PATH_CLEAR_CACHES))
      .andExpect(status().isNoContent());

    // then
    assertThat(specRulesCache.get(ruleCacheKey)).isNull();
    assertThat(profilesCache.get(profileCacheKey)).isNull();
  }
}
