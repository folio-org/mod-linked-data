package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;

@IntegrationTest
class ResourceControllerSearchResourceIT extends ITBase {

  @Test
  void shouldSearchResourcesUsingInventoryId() throws Exception {
    // given
    var resource = resourceTestService.saveGraph(getSampleInstanceResource(null, null));
    var requestBuilder = post("/linked-data/resources/search")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content("""
        {
          "inventoryIds": ["%s"]
        }
        """.formatted(resource.getFolioMetadata().getInventoryId())
      );

    // when
    var resultActions = mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON));

    // then
    var isbnPath = "$[0].outgoingEdges[*].target.[?(@.label == 'isbn value')]";
    var statementOfRespPath = "$[0].doc['http://bibfra.me/vocab/library/statementOfResponsibility']";
    resultActions
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].types").isArray())
      .andExpect(jsonPath("$[0].types[0]").value("INSTANCE"))
      .andExpect(jsonPath("$[0].label").value("Primary: mainTitle Primary: subTitle"))
      .andExpect(jsonPath(statementOfRespPath).isArray())
      .andExpect(jsonPath(statementOfRespPath + "[0]").value("statement of responsibility"))
      .andExpect(jsonPath("$[0].outgoingEdges").isArray())
      .andExpect(jsonPath(isbnPath).isNotEmpty())
      .andExpect(jsonPath(isbnPath + ".doc['http://bibfra.me/vocab/lite/name'][0]").value("isbn value"))
      .andExpect(jsonPath(isbnPath + ".types").isArray())
      .andExpect(jsonPath(isbnPath + ".types[*]", hasItems("ID_ISBN", "IDENTIFIER")));
  }

  @Test
  void shouldReturnEmptyArrayForNonExistingInventoryId() throws Exception {
    var requestBuilder = post("/linked-data/resources/search")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content("""
        {
          "inventoryIds": ["non-existing-id-12345"]
        }
        """);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(0));
  }

}
