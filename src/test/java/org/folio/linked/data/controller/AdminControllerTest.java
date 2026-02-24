package org.folio.linked.data.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.service.CacheService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

  @Mock
  private CacheService cacheService;

  @Test
  void clearCaches_shouldDelegateToServiceAndReturnNoContent() throws Exception {
    // given
    var mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(cacheService)).build();

    // when
    mockMvc.perform(delete("/linked-data/admin/caches"))
      .andExpect(status().isNoContent());

    // then
    verify(cacheService).clearCaches();
  }
}
