package org.folio.linked.data.e2e;

import static java.lang.System.getProperty;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.test.TestUtil.FOLIO_OKAPI_URL;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.repo.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
class HubControllerIT {

  private static final String HUB_ENDPOINT = "/linked-data/hub";
  @Autowired
  private Environment env;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepository;

  @Test
  void previewHub_shouldReturnHubResource_whenValidUriProvided() throws Exception {
    //given
    var hubCountBefore = resourceRepository.findAllByType(HUB.getUri(), Pageable.unpaged()).getTotalElements();
    var requestBuilder = MockMvcRequestBuilders.get(HUB_ENDPOINT)
      .param("hubUri", getHubUri())
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var result = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(result).contains("Hub 150986 mainTitle");

    var hubCountAfter = resourceRepository.findAllByType(HUB.getUri(), Pageable.unpaged()).getTotalElements();
    assertEquals(hubCountBefore, hubCountAfter);
  }

  @Test
  void saveHub_shouldSaveAndReturnHubResource_whenValidUriProvided() throws Exception {
    //given
    var hubCountBefore = resourceRepository.findAllByType(HUB.getUri(), Pageable.unpaged()).getTotalElements();
    var requestBuilder = MockMvcRequestBuilders.post(HUB_ENDPOINT)
      .param("hubUri", getHubUri())
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    var result = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(result).contains("Hub 150986 mainTitle");

    var hubCountAfter = resourceRepository.findAllByType(HUB.getUri(), Pageable.unpaged()).getTotalElements();
    assertThat(hubCountAfter).isEqualTo(hubCountBefore + 1);
  }

  private String getHubUri() {
    return getProperty(FOLIO_OKAPI_URL) + "/some-hub-storage/150986.json";
  }
}
