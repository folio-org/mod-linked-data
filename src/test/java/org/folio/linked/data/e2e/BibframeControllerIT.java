package org.folio.linked.data.e2e;

import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.getOkapiMockUrl;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";
  @Autowired
  private MockMvc mockMvc;

  @Test
  void createBibframeEndpoint_shouldStoreEntityCorrectly() throws Exception {
    String graphName = "graphName";
    String configuration = "{}";

    // given
    BibframeCreateRequest bibframeCreateRequest = new BibframeCreateRequest();
    bibframeCreateRequest.setGraphName(graphName);
    bibframeCreateRequest.setConfiguration(configuration);
    MockHttpServletRequestBuilder requestBuilder = post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(getOkapiMockUrl()))
      .content(asJsonString(bibframeCreateRequest));

    // when
    ResultActions resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("graphName").value(graphName))
      .andExpect(jsonPath("graphHash", notNullValue()))
      .andExpect(jsonPath("slug", notNullValue()))
      .andExpect(jsonPath("configuration").value(configuration));
  }
}
