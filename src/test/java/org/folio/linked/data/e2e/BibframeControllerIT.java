package org.folio.linked.data.e2e;

import static org.folio.linked.data.TestUtil.CONFIGURATION;
import static org.folio.linked.data.TestUtil.GRAPH_NAME;
import static org.folio.linked.data.TestUtil.OBJECT_MAPPER;
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

import java.util.UUID;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private BibframeRepository bibframeRepository;

  @Test
  void createBibframeEndpoint_shouldStoreEntityCorrectly() throws Exception {
    // given
    var bibframeCreateRequest = new BibframeCreateRequest();
    bibframeCreateRequest.setGraphName(GRAPH_NAME);
    bibframeCreateRequest.setConfiguration(CONFIGURATION);
    MockHttpServletRequestBuilder requestBuilder = post(BIBFRAMES_URL)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()))
        .content(asJsonString(bibframeCreateRequest));

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
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var bibframe = Bibframe.of(GRAPH_NAME, OBJECT_MAPPER.readTree(CONFIGURATION));
    var persisted = bibframeRepository.save(bibframe);

    var requestBuilder = get(BIBFRAMES_URL + "/" + persisted.getSlug())
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("id").value(persisted.getId()))
        .andExpect(jsonPath("graphName", equalTo(persisted.getGraphName())))
        .andExpect(jsonPath("graphHash").value(persisted.getGraphHash()))
        .andExpect(jsonPath("slug", equalTo(persisted.getSlug())))
        .andExpect(jsonPath("configuration", equalTo(persisted.getConfiguration().toString())));
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
}
