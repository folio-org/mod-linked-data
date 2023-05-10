package org.folio.linked.data.e2e;

import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.randomId;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";
  private static MockMvc mockMvc;
  private static OkapiConfiguration okapi;

  @BeforeAll
  static void prepare(@Autowired MockMvc mockMvc) throws Exception {
    BibframeControllerIT.mockMvc = mockMvc;
    mockMvc.perform(post("/_/tenant", randomId())
        .content(asJsonString(new TenantAttributes().moduleTo("mod-linked-data")))
        .headers(defaultHeaders(okapi.getOkapiUrl()))
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  @AfterAll
  static void cleanUp() throws Exception {
    mockMvc.perform(post("/_/tenant", randomId())
        .content(asJsonString(new TenantAttributes().moduleFrom("mod-search").purge(true)))
        .headers(defaultHeaders(okapi.getOkapiUrl())))
      .andExpect(status().isNoContent());
  }

  @Test
  void createBibframeEndpoint_shouldStoreEntityCorrectly() throws Exception {
    // given
    BibframeCreateRequest bibframeCreateRequest = new BibframeCreateRequest();
    bibframeCreateRequest.setToBeFilled(true);
    MockHttpServletRequestBuilder requestBuilder = post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(okapi.getOkapiUrl()))
      .content(asJsonString(bibframeCreateRequest));


    // when
    ResultActions resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("to-be-filled", is(true)));
  }
}
