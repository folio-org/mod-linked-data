package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProfileControllerIT {

  private static final String PROFILE_URL = "/linked-data/profile";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ProfileRepository profileRepository;
  @Autowired
  private Environment env;
  @MockBean
  private KafkaAdminService kafkaAdminService;

  @Order(1)
  @Test
  void getProfile_shouldReturnProfile() throws Exception {
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

    assertTrue(profile.contains("BIBFRAME 2.0"));
  }

  @Order(2)
  @Test
  void getProfile_shouldReturn404_ifNoProfileExists() throws Exception {
    //given
    var requestBuilder = get(PROFILE_URL)
      .headers(defaultHeaders(env));

    profileRepository.deleteAll();

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message",
        equalTo("Profile not found by id: [1] in Linked Data storage")))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }
}
