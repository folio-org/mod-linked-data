package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;

@IntegrationTest
class ResourceControllerInventoryIT extends ITBase {

  @Test
  void getResourceIdByResourceInventoryId_shouldReturnResourceId() throws Exception {
    //given
    var resource = resourceTestService.saveGraph(getSampleInstanceResource(null, null));
    var requestBuilder = get(RESOURCE_URL + "/metadata/" + resource.getFolioMetadata().getInventoryId() + "/id")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", equalTo(String.valueOf(resource.getId()))));
  }

  @Test
  void getResourceIdByResourceInventoryId_shouldReturn404_ifNoEntityExistsWithGivenInventoryId() throws Exception {
    //given
    var inventoryId = UUID.randomUUID();
    var requestBuilder = get(RESOURCE_URL + "/metadata/" + inventoryId + "/id")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    //when
    var resultActions = mockMvc.perform(requestBuilder);

    //then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message",
        equalTo("Resource not found by inventoryId: [" + inventoryId + "] in Linked Data storage")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }
}
