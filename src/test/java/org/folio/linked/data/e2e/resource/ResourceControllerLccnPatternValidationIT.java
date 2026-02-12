package org.folio.linked.data.e2e.resource;

import static java.util.UUID.randomUUID;
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
import static org.folio.linked.data.test.resource.ResourceSpecUtil.createSpecRules;
import static org.folio.linked.data.test.resource.ResourceSpecUtil.createSpecifications;
import static org.folio.linked.data.test.resource.ResourceUtils.setExistingResourcesIds;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
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
import org.folio.linked.data.integration.rest.specification.SpecClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class ResourceControllerLccnPatternValidationIT extends ITBase {

  @MockitoBean
  private SpecClient specClient;
  @MockitoBean
  private SearchClient searchClient;

  @Test
  void createInstanceWithWorkRef_shouldReturn400_ifLccnIsInvalid() throws Exception {
    // given
    var specRuleId = randomUUID();
    when(specClient.getBibMarcSpecs()).thenReturn(ResponseEntity.ok().body(createSpecifications(specRuleId)));
    when(specClient.getSpecRules(specRuleId)).thenReturn(ResponseEntity.ok().body(createSpecRules()));

    var work = getSampleWork(null);
    setExistingResourcesIds(work, hashService);
    resourceTestService.saveGraph(work);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(INSTANCE_WITH_WORK_REF_SAMPLE
        .replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString())
        .replace("http://id/lccn", "http://id.loc.gov/vocabulary/mstatus/current")
      );
    when(searchClient.searchInstances(any()))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(0L), HttpStatus.OK));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("lccn_does_not_match_pattern")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(2)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void update_shouldReturn400_ifLccnIsInvalid() throws Exception {
    // given
    var specRuleId = randomUUID();
    when(specClient.getBibMarcSpecs()).thenReturn(ResponseEntity.ok().body(createSpecifications(specRuleId)));
    when(specClient.getSpecRules(specRuleId)).thenReturn(ResponseEntity.ok().body(createSpecRules()));

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
      );
    when(searchClient.searchInstances(any()))
      .thenReturn(new ResponseEntity<>(new SearchResponseTotalOnly().totalRecords(0L), HttpStatus.OK));

    // when
    var resultActions = mockMvc.perform(updateRequest);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].code", equalTo("lccn_does_not_match_pattern")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(2)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }
}
