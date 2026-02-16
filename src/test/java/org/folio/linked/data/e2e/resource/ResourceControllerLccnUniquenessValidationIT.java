package org.folio.linked.data.e2e.resource;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.WORK_ID_PLACEHOLDER;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.MonographTestUtil.setCurrentStatus;
import static org.folio.linked.data.test.TestUtil.INSTANCE_WITH_WORK_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getSampleInstanceDtoMap;
import static org.folio.linked.data.test.resource.ResourceUtils.setExistingResourcesIds;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.rest.search.SearchClient;
import org.folio.linked.data.integration.rest.settings.SettingsService;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerLccnUniquenessValidationIT extends ITBase {

  private static final String LCCN_VALIDATION_NOT_AVAILABLE =
    "[Could not validate LCCN for duplicate] - reason: [Unable to reach search service]. Please try later.";

  @MockitoBean
  private SearchClient searchClient;
  @MockitoBean
  private SettingsService settingsService;

  @Test
  void createInstanceWithWorkRef_shouldReturn400_ifLccnIsNotUnique() throws Exception {
    // given
    var work = getSampleWork(null);
    setExistingResourcesIds(work, hashService);
    resourceTestService.saveGraph(work);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE
        .replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
        .replace("http://id/lccn", "http://id.loc.gov/vocabulary/mstatus/current")
        .replace("lccn value", "nn0123456789")
      );
    var query = "(lccn==\"nn0123456789\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
    when(searchClient.searchInstances(query))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), HttpStatus.OK));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("lccn_not_unique")))
      .andExpect(jsonPath("total_records", equalTo(1)));
    verify(searchClient).searchInstances(query);
  }

  @Test
  void update_shouldReturn400_ifLccnIsNotUnique() throws Exception {
    // given
    var updateDto = getSampleInstanceDtoMap();
    var instance = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(INSTANCE.getUri());
    instance.remove("inventoryId");
    instance.remove("srsId");
    setCurrentStatus(instance);
    var work = getSampleWork(null);
    var originalInstance = resourceTestService.saveGraph(getSampleInstanceResource(null, work));

    var updateRequest = put(RESOURCE_URL + "/" + originalInstance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        TEST_JSON_MAPPER.writeValueAsString(updateDto).replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
          .replace("lccn value", "nn0123456789")
      );
    var query = "(lccn==\"nn0123456789\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")"
      + " and id <> \"2165ef4b-001f-46b3-a60e-52bcdeb3d5a1\"";
    when(searchClient.searchInstances(query))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(1L), HttpStatus.OK));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("lccn_not_unique")))
      .andExpect(jsonPath("total_records", equalTo(1)));
    verify(searchClient).searchInstances(query);
  }

  @Test
  void createInstanceWithWorkRef_shouldSuccess_ifLccnDeduplicationDisabled() throws Exception {
    // given
    var work = getSampleWork(null);
    setExistingResourcesIds(work, hashService);
    resourceTestService.saveGraph(work);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE
        .replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
        .replace("http://id/lccn", "http://id.loc.gov/vocabulary/mstatus/current")
        .replace("lccn value", "nn0123456789")
      );
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(false);

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON));
  }

  @Test
  void createInstanceWithWorkRef_shouldReturn424_ifSearchServiceThrownException() throws Exception {
    // given
    var work = getSampleWork(null);
    setExistingResourcesIds(work, hashService);
    resourceTestService.saveGraph(work);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE
        .replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
        .replace("http://id/lccn", "http://id.loc.gov/vocabulary/mstatus/current")
        .replace("lccn value", "nn0123456789")
      );
    var query = "(lccn==\"nn0123456789\") and (staffSuppress <> \"true\" and discoverySuppress <> \"true\")";
    when(searchClient.searchInstances(any())).thenThrow(new RuntimeException());
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isFailedDependency())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("failed_dependency")))
      .andExpect(jsonPath("errors[0].message", equalTo(LCCN_VALIDATION_NOT_AVAILABLE)))
      .andExpect(jsonPath("total_records", equalTo(1)));
    verify(searchClient).searchInstances(query);
  }
}
