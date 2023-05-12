package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.getOkapiMockUrl;
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
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private BibframeRepo bibframeRepo;

  @Test
  void createBibframeEndpoint_shouldStoreEntityCorrectlyAndReturnIt() throws Exception {
    // given
    var bibframeCreateRequest = new BibframeCreateRequest();
    bibframeCreateRequest.setToBeFilled(true);
    var requestBuilder = post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(getOkapiMockUrl()))
      .content(asJsonString(bibframeCreateRequest));


    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var mvcResult = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("to-be-filled", is(true)))
      .andReturn();
    String id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "id");
    var bibframeOptional = bibframeRepo.read(UUID.fromString(id));
    assertThat(bibframeOptional).isNotEmpty();
    assertThat(bibframeOptional.get().isToBeFilled()).isEqualTo(bibframeCreateRequest.getToBeFilled());
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var bibframe = Bibframe.of(null, true);
    var persisted = bibframeRepo.persist(bibframe);

    var requestBuilder = get(BIBFRAMES_URL + "/" + persisted.getId().toString())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(getOkapiMockUrl()));


    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", is(persisted.getId().toString())))
      .andExpect(jsonPath("to-be-filled", is(persisted.isToBeFilled())));
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
