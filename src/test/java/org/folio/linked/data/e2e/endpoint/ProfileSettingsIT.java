package org.folio.linked.data.e2e.endpoint;

import static java.util.UUID.randomUUID;
import static org.folio.linked.data.test.TestUtil.defaultHeadersWithUserId;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ProfileSettingsIT {

  private static final String PROFILE_URL = "/linked-data/profile/";
  private static final String SETTINGS_PATH = "/settings";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;

  @Test
  void shouldBeEmptyForProfileWithNoSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    // when, then
    var getAllRequest = get(PROFILE_URL + "2" + SETTINGS_PATH)
      .headers(headers);
    mockMvc.perform(getAllRequest)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldBeNotFoundForUnknownProfile() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    // when, then
    var getRequest = get(PROFILE_URL + "9999999" + SETTINGS_PATH + "/1")
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
    var getRequest = get(PROFILE_URL + "2" + SETTINGS_PATH + "/1")
      .headers(headers);

    // then
    mockMvc.perform(getRequest)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.active", is(false)));
  }

  @Test
  void shouldCreateProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    var postRequest = post(PROFILE_URL + "2" + SETTINGS_PATH)
      .headers(headers)
      .content("""
        {
          "name": "My settings",
          "active": true,
          "children": [
            {
              "id": "Work:Monograph:Title",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var postResult = mockMvc.perform(postRequest)
      .andExpect(status().isCreated())
      .andReturn();
    var postResultBody = postResult.getResponse().getContentAsString();
    var settingsId = JsonPath.read(postResultBody, "$.id");

    // when
    var getRequest = get(PROFILE_URL + "2" + SETTINGS_PATH + "/" + settingsId)
      .headers(headers);

    // then
    mockMvc.perform(getRequest)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.active", is(true)))
      .andExpect(jsonPath("$.children.length()", equalTo(1)));
  }

  @Test
  void shouldSetProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    var postRequest = post(PROFILE_URL + "2" + SETTINGS_PATH)
      .headers(headers)
      .content("""
        {
          "name": "My settings",
          "active": true,
          "children": [
            {
              "id": "Work:Monograph:Title",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var postResult = mockMvc.perform(postRequest)
      .andExpect(status().isCreated())
      .andReturn();
    var postResultBody = postResult.getResponse().getContentAsString();
    var settingsId = JsonPath.read(postResultBody, "$.id");

    // when
    var putRequest = put(PROFILE_URL + "2" + SETTINGS_PATH + "/" + settingsId)
      .headers(headers)
      .content("""
        {
          "name": "My settings",
          "active": true,
          "children": [
            {
              "id": "Work:Monograph:Title",
              "visible": true,
              "order": 1
            },
            {
              "id": "Work:Monograph:OtherTitleInformation",
              "visible": true,
              "order": 2
            }
          ]
        }
          """);
    mockMvc.perform(putRequest)
      .andExpect(status().isNoContent());

    var getRequest = get(PROFILE_URL + "2" + SETTINGS_PATH + "/" + settingsId)
      .headers(headers);

    // then
    mockMvc.perform(getRequest)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.active", is(true)))
      .andExpect(jsonPath("$.children.length()", equalTo(2)));
  }

  @Test
  void shouldDeleteProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    var postRequest = post(PROFILE_URL + "2" + SETTINGS_PATH)
      .headers(headers)
      .content("""
        {
          "name": "My settings",
          "active": true,
          "children": [
            {
              "id": "Work:Monograph:Title",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var postResult = mockMvc.perform(postRequest)
      .andExpect(status().isCreated())
      .andReturn();
    var postResultBody = postResult.getResponse().getContentAsString();
    var settingsId = JsonPath.read(postResultBody, "$.id");

    // when
    var deleteRequest = delete(PROFILE_URL + "2" + SETTINGS_PATH + "/" + settingsId)
      .headers(headers);
    mockMvc.perform(deleteRequest)
      .andExpect(status().isNoContent());
  }
}
