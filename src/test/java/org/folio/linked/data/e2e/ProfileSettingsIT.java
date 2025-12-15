package org.folio.linked.data.e2e;

import static java.util.UUID.randomUUID;
import static org.folio.linked.data.test.TestUtil.defaultHeadersWithUserId;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

@IntegrationTest
class ProfileSettingsIT {

  private static final String PROFILE_SETTINGS_URL = "/linked-data/profile/settings/";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;

  @Test
  void shouldBeNotFoundForUnknownProfile() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    // when, then
    var getRequest = get(PROFILE_SETTINGS_URL + "9999999")
      .headers(headers);
    mockMvc.perform(getRequest)
      .andExpect(status().isNotFound());
  }

  @Test
  void shouldBeInactiveSettingsWhenNotSet() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    // when
    var getRequest = get(PROFILE_SETTINGS_URL + "2")
      .headers(headers);

    // then
    mockMvc.perform(getRequest)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.active", is(false)));
  }

  @Test
  void shouldSetProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    var postRequest = post(PROFILE_SETTINGS_URL + "2")
      .headers(headers)
      .content("""
        {
            "active": true,
            "children": [
              {
                "id": "Work:Monograph:Title",
                "visible": true,
                "order": 1
              }
            ]
        }""");
    mockMvc.perform(postRequest)
      .andExpect(status().isNoContent());

    // when 
    var getRequest = get(PROFILE_SETTINGS_URL + "2")
      .headers(headers);

    // then
    mockMvc.perform(getRequest)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.active", is(true)))
      .andExpect(jsonPath("$.children.length()", equalTo(1)));
  }
}
