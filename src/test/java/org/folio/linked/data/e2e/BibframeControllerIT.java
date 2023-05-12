package org.folio.linked.data.e2e;

import static org.folio.linked.data.TestUtil.CONFIGURATION;
import static org.folio.linked.data.TestUtil.GRAPH_NAME;
import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.getOkapiMockUrl;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void createBibframe_shouldStoreEntityCorrectly() throws Exception {
    // given
    var requestBuilder = getCreateRequestBuilder();

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("graphName", is(GRAPH_NAME)))
      .andExpect(jsonPath("graphHash", notNullValue()))
      .andExpect(jsonPath("slug", notNullValue()))
      .andExpect(jsonPath("configuration", equalTo(CONFIGURATION)));
  }

  @Test
  void createAndGetBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var createResult = mockMvc.perform(getCreateRequestBuilder()).andReturn();
    var slug = JsonPath.read(createResult.getResponse().getContentAsString(), "slug");

    var requestBuilder = get(BIBFRAMES_URL + "/" + slug)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("graphName", equalTo(GRAPH_NAME)))
      .andExpect(jsonPath("graphHash").isNotEmpty())
      .andExpect(jsonPath("slug", equalTo(slug)))
      .andExpect(jsonPath("configuration", equalTo(CONFIGURATION)));
  }

  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = UUID.randomUUID().toString();
    var requestBuilder = get(BIBFRAMES_URL + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNotFound());
  }

  @NotNull
  private MockHttpServletRequestBuilder getCreateRequestBuilder() {
    var bibframeCreateRequest = new BibframeCreateRequest();
    bibframeCreateRequest.setGraphName(GRAPH_NAME);
    bibframeCreateRequest.setConfiguration(CONFIGURATION);
    return post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(getOkapiMockUrl()))
      .content(asJsonString(bibframeCreateRequest));
  }
}
