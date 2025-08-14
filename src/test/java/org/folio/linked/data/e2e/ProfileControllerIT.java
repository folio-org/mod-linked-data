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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest
  @CsvSource({
    "2, lde:Profile:Work,     http://bibfra.me/vocab/lite/Work",
    "3, lde:Profile:Instance, http://bibfra.me/vocab/lite/Instance",
    "4, lde:Profile:Instance, http://bibfra.me/vocab/lite/Instance",
    "5, lde:Profile:Instance, http://bibfra.me/vocab/lite/Instance",
  })
  void getProfileById_returnsProfile(long id, String expectedId, String expectedResourceType) throws Exception {
    //given
    var requestBuilder = get(PROFILE_URL + "/" + id)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var profile = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(TEXT_PLAIN_VALUE + ";charset=UTF-8"))
      .andReturn().getResponse().getContentAsString();

    assertTrue(profile.contains("\"bfid\": \"" + expectedId + "\""));
    assertTrue(profile.contains("\"uriBFLite\": \"" + expectedResourceType + "\""));
  }

  @Test
  void getProfileById_returnsNotFound() throws Exception {
    //given
    var nonExistingProfileId = 1L;
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
    var requestBuilder = get(PROFILE_URL + "/metadata?resourceType=http://bibfra.me/vocab/lite/Work")
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id", equalTo(2)))
      .andExpect(jsonPath("$[0].name", equalTo("Work")))
      .andExpect(jsonPath("$[0].resourceType", equalTo("http://bibfra.me/vocab/lite/Work")));
  }
}
