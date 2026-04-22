package org.folio.linked.data.e2e.endpoint;

import static java.lang.System.getProperty;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.test.TestUtil.FOLIO_OKAPI_URL;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.test.resource.ResourceTestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
  private ResourceTestRepository resourceRepository;

  @Test
  void previewHub_shouldReturnHubResource_whenValidUriProvided() throws Exception {
    //given
    var hubCountBefore = resourceRepository.findAllByTypeWithEdgesLoaded(Set.of(HUB.getUri()), 1, unpaged())
      .getTotalElements();
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

    var hubCountAfter = resourceRepository.findAllByTypeWithEdgesLoaded(Set.of(HUB.getUri()), 1, unpaged())
      .getTotalElements();
    assertEquals(hubCountBefore, hubCountAfter);
  }

  @Test
  void saveHub_shouldSaveAndReturnHubResource_whenValidUriProvided() throws Exception {
    //given
    var hubCountBefore = resourceRepository.findAllByTypeWithEdgesLoaded(Set.of(HUB.getUri()), 1, unpaged())
      .getTotalElements();
    var requestBuilder = MockMvcRequestBuilders.post(HUB_ENDPOINT)
      .param("hubUri", getHubUri())
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.resource['http://bibfra.me/vocab/lite/Hub']"
        + "['http://bibfra.me/vocab/library/title'][0]"
        + "['http://bibfra.me/vocab/library/Title']['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value("Hub 150986 mainTitle 汉"));

    var hubCountAfter = resourceRepository.findAllByTypeWithEdgesLoaded(Set.of(HUB.getUri()), 1, unpaged())
      .getTotalElements();
    assertThat(hubCountAfter).isEqualTo(hubCountBefore + 1);
  }

  @Test
  void saveHub_shouldReturnFullHubResource_whenSavedSecondTime() throws Exception {
    //given
    var requestBuilder = MockMvcRequestBuilders.post(HUB_ENDPOINT)
      .param("hubUri", getSecondHubUri())
      .headers(defaultHeaders(env));
    mockMvc.perform(requestBuilder).andExpect(status().isOk());
    var hubCountAfterFirstSave = resourceRepository.findAllByTypeWithEdgesLoaded(Set.of(HUB.getUri()), 1, unpaged())
      .getTotalElements();

    //when saved a second time (updates an existing hub)
    var resultActions = mockMvc.perform(requestBuilder);

    //then the response still contains fully mapped edges (no LazyInitializationException)
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$.resource['http://bibfra.me/vocab/lite/Hub']"
        + "['http://bibfra.me/vocab/library/title'][0]"
        + "['http://bibfra.me/vocab/library/Title']['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value("Hub 150987 mainTitle"));
    var hubCountAfterSecondSave = resourceRepository.findAllByTypeWithEdgesLoaded(Set.of(HUB.getUri()), 1, unpaged())
      .getTotalElements();
    assertThat(hubCountAfterSecondSave).isEqualTo(hubCountAfterFirstSave);
  }

  private String getHubUri() {
    return getProperty(FOLIO_OKAPI_URL) + "/some-hub-storage/150986.json";
  }

  private String getSecondHubUri() {
    return getProperty(FOLIO_OKAPI_URL) + "/some-hub-storage/150987.json";
  }
}
