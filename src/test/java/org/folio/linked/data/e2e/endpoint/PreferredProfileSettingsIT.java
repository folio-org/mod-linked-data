package org.folio.linked.data.e2e.endpoint;

import static java.util.UUID.randomUUID;
import static org.folio.linked.data.test.TestUtil.defaultHeadersWithUserId;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class PreferredProfileSettingsIT {
  private static final String PREFERRED_PROFILE_SETTINGS_URL = "/linked-data/profile/3/preferred";
  private static final String PROFILE_SETTINGS_URL = "/linked-data/profile/3/settings";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;

  @Test
  void shouldSetPreferredProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    var createProfileSettingsRequest = post(PROFILE_SETTINGS_URL)
      .headers(headers)
      .content("""
        {
          "name": "My settings",
          "active": true,
          "children": [
            {
              "id": "Profile:Resource:Property",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var createProfileSettingsResult = mockMvc.perform(createProfileSettingsRequest)
      .andExpect(status().isCreated())
      .andReturn();
    var createProfileSettingsResultBody = createProfileSettingsResult.getResponse().getContentAsString();
    var settingsId = JsonPath.read(createProfileSettingsResultBody, "$.id");

    // when
    var postRequest = post(PREFERRED_PROFILE_SETTINGS_URL)
      .headers(headers)
      .content("""
        {
            "profileSettingsId": %d
        }""".formatted(settingsId));
    mockMvc.perform(postRequest)
      .andExpect(status().isNoContent());


    // then
    validatePreferredProfileSettings(mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL).headers(headers)));
    validatePreferredProfileSettings(
      mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL)
        .headers(headers))
    );
  }

  @Test
  void shouldReturnEmptyPreferredProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());

    // when
    validateEmptyPreferredProfileSettings(mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL).headers(headers)));
  }

  @Test
  void shouldDeletePreferredProfileSettings() throws Exception {
    // given
    var headers = defaultHeadersWithUserId(env, randomUUID().toString());
    headers.setContentType(APPLICATION_JSON);

    var createProfileSettingsRequest = post(PROFILE_SETTINGS_URL)
      .headers(headers)
      .content("""
        {
          "name": "My settings",
          "active": true,
          "children": [
            {
              "id": "Profile:Resource:Property",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var createProfileSettingsResult = mockMvc.perform(createProfileSettingsRequest)
      .andExpect(status().isCreated())
      .andReturn();
    var createProfileSettingsResultBody = createProfileSettingsResult.getResponse().getContentAsString();
    var settingsId = JsonPath.read(createProfileSettingsResultBody, "$.id");
    
    var postRequest = post(PREFERRED_PROFILE_SETTINGS_URL)
      .headers(headers)
      .content("""
        {
            "profileSettingsId": %d
        }""".formatted(settingsId));
    mockMvc.perform(postRequest)
      .andExpect(status().isNoContent());

    validatePreferredProfileSettings(mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL).headers(headers)));

    // when
    mockMvc.perform(delete(PREFERRED_PROFILE_SETTINGS_URL)
      .headers(headers))
      .andExpect(status().isNoContent());

    // then
    validateEmptyPreferredProfileSettings(mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL).headers(headers)));
  }

  @Test
  void shouldPreferredProfileSettingsDifferPerUser() throws Exception {
    // given
    var headersUser1 = defaultHeadersWithUserId(env, randomUUID().toString());
    headersUser1.setContentType(APPLICATION_JSON);
    var headersUser2 = defaultHeadersWithUserId(env, randomUUID().toString());
    headersUser2.setContentType(APPLICATION_JSON);

    var createProfileSettingsRequestUser1 = post(PROFILE_SETTINGS_URL)
      .headers(headersUser1)
      .content("""
        {
          "name": "settings for user 1",
          "active": true,
          "children": [
            {
              "id": "Profile:Resource:Property",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var createProfileSettingsResultUser1 = mockMvc.perform(createProfileSettingsRequestUser1)
      .andExpect(status().isCreated())
      .andReturn();
    var createProfileSettingsResultBodyUser1 = createProfileSettingsResultUser1.getResponse().getContentAsString();
    var settingsIdUser1 = JsonPath.read(createProfileSettingsResultBodyUser1, "$.id");

    var createProfileSettingsRequestUser2 = post(PROFILE_SETTINGS_URL)
      .headers(headersUser2)
      .content("""
        {
          "name": "user 2 settings",
          "active": true,
          "children": [
            {
              "id": "Profile:Resource:Property",
              "visible": true,
              "order": 1
            }
          ]
        }""");
    var createProfileSettingsResultUser2 = mockMvc.perform(createProfileSettingsRequestUser2)
      .andExpect(status().isCreated())
      .andReturn();
    var createProfileSettingsResultBodyUser2 = createProfileSettingsResultUser2.getResponse().getContentAsString();
    var settingsIdUser2 = JsonPath.read(createProfileSettingsResultBodyUser2, "$.id");

    // when: users set their own settings as preferred for profile 3
    mockMvc.perform(post(PREFERRED_PROFILE_SETTINGS_URL)
        .headers(headersUser1)
        .content("""
          {
              "profileSettingsId": %d
          }""".formatted(settingsIdUser1)))
      .andExpect(status().isNoContent());

    mockMvc.perform(post(PREFERRED_PROFILE_SETTINGS_URL)
        .headers(headersUser2)
        .content("""
          {
              "profileSettingsId": %d
          }""".formatted(settingsIdUser2)))
      .andExpect(status().isNoContent());

    // then: each user sees only their own preferred profile settings
    mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL).headers(headersUser1))
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.length()", equalTo(1)))
      .andExpect(jsonPath("$[0].id", equalTo(settingsIdUser1)))
      .andExpect(jsonPath("$[0].name", equalTo("settings for user 1")));

    mockMvc.perform(get(PREFERRED_PROFILE_SETTINGS_URL).headers(headersUser2))
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.length()", equalTo(1)))
      .andExpect(jsonPath("$[0].id", equalTo(settingsIdUser2)))
      .andExpect(jsonPath("$[0].name", equalTo("user 2 settings")));
  }

  private void validatePreferredProfileSettings(ResultActions result) throws Exception {
    result
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$[0].name", equalTo("My settings")))
      .andExpect(jsonPath("$[0].profileId", equalTo(3)))
      .andExpect(jsonPath("$.length()", equalTo(1)));
  }

  private void validateEmptyPreferredProfileSettings(ResultActions result) throws Exception {
    result
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()", equalTo(0)));
  }
}
