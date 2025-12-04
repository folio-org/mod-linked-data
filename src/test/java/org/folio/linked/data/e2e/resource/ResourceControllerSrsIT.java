package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.INSTANCE_ID_PLACEHOLDER;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.SIMPLE_WORK_WITH_INSTANCE_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.resource.ResourceJsonPath.toCreatorReferenceId;
import static org.folio.linked.data.test.resource.ResourceUtils.setExistingResourcesIds;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.rest.srs.SrsClient;
import org.folio.linked.data.test.TestUtil;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class ResourceControllerSrsIT extends ITBase {

  @MockitoBean
  private SrsClient srsClient;

  @Test
  void createWorkWithInstanceRef_shouldCreateAuthorityFromSrs() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference, hashService);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        SIMPLE_WORK_WITH_INSTANCE_REF_SAMPLE
          .replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    when(srsClient.getAuthorityBySrsId("4f2220d5-ddf6-410a-a459-cd4b5e1b5ddd"))
      .thenReturn(new ResponseEntity<>(createRecord(), HttpStatusCode.valueOf(200)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(toCreatorReferenceId(), equalTo("-2642702223879770981")));
  }

  @Test
  void createWorkWithInstanceRef_shouldReturn404_ifRecordNotFoundInSrs() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference, hashService);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        SIMPLE_WORK_WITH_INSTANCE_REF_SAMPLE
          .replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    when(srsClient.getAuthorityBySrsId("4f2220d5-ddf6-410a-a459-cd4b5e1b5ddd"))
      .thenReturn(new ResponseEntity<>(null, HttpStatusCode.valueOf(404)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("errors[0].message",
        equalTo("Source Record not found by srsId: [4f2220d5-ddf6-410a-a459-cd4b5e1b5ddd] in Source Record storage")))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  private org.folio.rest.jaxrs.model.Record createRecord() {
    var content = TestUtil.loadResourceAsString("samples/marc2ld/marc_authority.jsonl");
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }
}
