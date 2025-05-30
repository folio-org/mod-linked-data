package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTestStandalone;
import org.junit.jupiter.api.Test;

@IntegrationTestStandalone
class ResourceControllerMarcPreviewIT extends AbstractResourceControllerIT {

  @Test
  void getResourceViewById_shouldReturnInstance() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResource());
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getId() + "/marc")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    resultActions
      .andExpect(jsonPath("id", equalTo(existed.getId().toString())))
      .andExpect(jsonPath("recordType", equalTo("MARC_BIB")))
      .andExpect(jsonPath("parsedRecord.content", notNullValue()));
  }
}
