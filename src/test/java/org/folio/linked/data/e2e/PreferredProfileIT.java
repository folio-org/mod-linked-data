package org.folio.linked.data.e2e;

import static java.util.UUID.randomUUID;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class PreferredProfileIT {

  private static final String PREFERRED_PROFILE_URL = "/linked-data/profile/preferred";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;

  @Test
  void shouldSetPreferredProfile() throws Exception {
    // given
    var headers = defaultHeaders(env);
    headers.add("x-okapi-user-id", randomUUID().toString());

    // when
    var postRequest = post(PREFERRED_PROFILE_URL)
      .headers(headers)
      .content("""
        {
            "id": 1,
            "resourceType": "http://bibfra.me/vocab/lite/Instance"
        }""");
    mockMvc.perform(postRequest)
      .andExpect(status().isNoContent());


    // then
    validatePreferredProfile(mockMvc.perform(get(PREFERRED_PROFILE_URL).headers(headers)));
    validatePreferredProfile(
      mockMvc.perform(get(PREFERRED_PROFILE_URL + "?resourceType=http://bibfra.me/vocab/lite/Instance")
        .headers(headers))
    );
    validateEmptyPreferredProfile(
      mockMvc.perform(get(PREFERRED_PROFILE_URL + "?resourceType=http://bibfra.me/vocab/lite/Work")
        .headers(headers))
    );
  }

  @Test
  void shouldReturnEmptyPreferredProfile() throws Exception {
    // given
    var headers = defaultHeaders(env);
    headers.add("x-okapi-user-id", randomUUID().toString());

    // when
    validateEmptyPreferredProfile(mockMvc.perform(get(PREFERRED_PROFILE_URL).headers(headers)));
  }

  private void validatePreferredProfile(ResultActions result) throws Exception {
    result
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id", equalTo(1)))
      .andExpect(jsonPath("$[0].name", equalTo("Monograph")))
      .andExpect(jsonPath("$[0].resourceType", equalTo("http://bibfra.me/vocab/lite/Instance")))
      .andExpect(jsonPath("$.length()", equalTo(1)));
  }

  private void validateEmptyPreferredProfile(ResultActions result) throws Exception {
    result
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()", equalTo(0)));
  }
}
