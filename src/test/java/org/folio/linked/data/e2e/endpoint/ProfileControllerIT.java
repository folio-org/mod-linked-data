package org.folio.linked.data.e2e.endpoint;

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
    "6, lde:Profile:Work,     http://bibfra.me/vocab/lite/Work",
    "7, lde:Profile:Hub,      http://bibfra.me/vocab/lite/Hub"
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
  void getMetadataByResourceType_returnsMetadata_forWork() throws Exception {
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
      .andExpect(jsonPath("$[0].name", equalTo("Books")))
      .andExpect(jsonPath("$[0].resourceType", equalTo("http://bibfra.me/vocab/lite/Work")))
      .andExpect(jsonPath("$[1].id", equalTo(6)))
      .andExpect(jsonPath("$[1].name", equalTo("Serials Work")))
      .andExpect(jsonPath("$[1].resourceType", equalTo("http://bibfra.me/vocab/lite/Work")));
  }

  @Test
  void getMetadataByResourceType_returnsMetadataForAuthority() throws Exception {
    //given
    var requestBuilder = get(PROFILE_URL + "/metadata?resourceType=http://bibfra.me/vocab/lite/Authority")
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id", equalTo(8)))
      .andExpect(jsonPath("$[0].name", equalTo("Family")))
      .andExpect(jsonPath("$[0].resourceType", equalTo("http://bibfra.me/vocab/lite/Family")))
      .andExpect(jsonPath("$[1].id", equalTo(9)))
      .andExpect(jsonPath("$[1].name", equalTo("Form")))
      .andExpect(jsonPath("$[1].resourceType", equalTo("http://bibfra.me/vocab/lite/Form")))
      .andExpect(jsonPath("$[2].id", equalTo(10)))
      .andExpect(jsonPath("$[2].name", equalTo("Jurisdiction")))
      .andExpect(jsonPath("$[2].resourceType", equalTo("http://bibfra.me/vocab/lite/Jurisdiction")))
      .andExpect(jsonPath("$[3].id", equalTo(11)))
      .andExpect(jsonPath("$[3].name", equalTo("Meeting")))
      .andExpect(jsonPath("$[3].resourceType", equalTo("http://bibfra.me/vocab/lite/Meeting")))
      .andExpect(jsonPath("$[4].id", equalTo(12)))
      .andExpect(jsonPath("$[4].name", equalTo("Organization")))
      .andExpect(jsonPath("$[4].resourceType", equalTo("http://bibfra.me/vocab/lite/Organization")))
      .andExpect(jsonPath("$[5].id", equalTo(13)))
      .andExpect(jsonPath("$[5].name", equalTo("Person")))
      .andExpect(jsonPath("$[5].resourceType", equalTo("http://bibfra.me/vocab/lite/Person")))
      .andExpect(jsonPath("$[6].id", equalTo(14)))
      .andExpect(jsonPath("$[6].name", equalTo("Place")))
      .andExpect(jsonPath("$[6].resourceType", equalTo("http://bibfra.me/vocab/lite/Place")))
      .andExpect(jsonPath("$[7].id", equalTo(15)))
      .andExpect(jsonPath("$[7].name", equalTo("Temporal")))
      .andExpect(jsonPath("$[7].resourceType", equalTo("http://bibfra.me/vocab/lite/Temporal")))
      .andExpect(jsonPath("$[8].id", equalTo(16)))
      .andExpect(jsonPath("$[8].name", equalTo("Topic")))
      .andExpect(jsonPath("$[8].resourceType", equalTo("http://bibfra.me/vocab/lite/Topic")));
  }
}
