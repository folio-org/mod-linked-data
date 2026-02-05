package org.folio.linked.data.e2e;

import static java.lang.System.getProperty;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.FOLIO_OKAPI_URL;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
class HubControllerIT {

  private static final String HUB_ENDPOINT = "/linked-data/hub";
  @Autowired
  private Environment env;
  @Autowired
  private MockMvc mockMvc;

  @Test
  void previewHub_shouldReturnHubResource_whenValidUriProvided() throws Exception {
    //given
    var requestBuilder = MockMvcRequestBuilders.get(HUB_ENDPOINT)
      .param("hubUri", getProperty(FOLIO_OKAPI_URL) + "/some-hub-storage/150986.json")
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var result = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(result).contains("Hub 150986 mainTitle");
  }
}
