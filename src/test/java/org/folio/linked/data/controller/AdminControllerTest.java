package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.util.Constants.Cache.PROFILES;
import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;
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

    var specRulesCache = cacheManager.getCache("spec-rules");
    assertThat(specRulesCache).isNotNull();
    specRulesCache.put(SPEC_RULES, "rule-value");
    assertThat(specRulesCache.get(SPEC_RULES)).isNotNull();

    var profilesCache = cacheManager.getCache("profiles");
    assertThat(profilesCache).isNotNull();
    profilesCache.put(PROFILES, "profile-value");
    assertThat(profilesCache.get(PROFILES)).isNotNull();

    var mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(cacheManager)).build();

    // when
    mockMvc.perform(delete("/linked-data/admin/caches"))
      .andExpect(status().isNoContent());

    // then
    assertThat(specRulesCache.get(SPEC_RULES)).isNull();
    assertThat(profilesCache.get(PROFILES)).isNull();
  }
}
