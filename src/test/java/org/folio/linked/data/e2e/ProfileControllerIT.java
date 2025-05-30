package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ProfileControllerIT {

  private static final String PROFILE_URL = "/linked-data/profile";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;

  @Test
  void getProfile_returnsProfile() throws Exception {
    //given
    var requestBuilder = get(PROFILE_URL)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var profile = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(TEXT_PLAIN_VALUE + ";charset=UTF-8"))
      .andReturn().getResponse().getContentAsString();

    assertTrue(profile.contains("\"id\": \"lc:RT:bf2:Monograph:Work\""));
  }

  @Test
  void getProfileById_returnsProfile() throws Exception {
    //given
    var requestBuilder = get(PROFILE_URL + "/1")
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var profile = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(TEXT_PLAIN_VALUE + ";charset=UTF-8"))
      .andReturn().getResponse().getContentAsString();

    assertTrue(profile.contains("\"id\": \"lc:RT:bf2:Monograph:Work\""));
  }

  @Test
  void getProfileById_returnsNotFound() throws Exception {
    //given
    var nonExistingProfileId = 0L;
    var requestBuilder = get(PROFILE_URL + "/" + nonExistingProfileId)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")));
  }

  @Test
  void getMetadataByResourceType_returnsMetadata() throws Exception {
    //given
    var requestBuilder = get(PROFILE_URL + "/metadata?resourceType=http://bibfra.me/vocab/lite/Instance")
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id", equalTo(1)))
      .andExpect(jsonPath("$[0].name", equalTo("Monograph")))
      .andExpect(jsonPath("$[0].resourceType", equalTo("http://bibfra.me/vocab/lite/Instance")));
  }
}
