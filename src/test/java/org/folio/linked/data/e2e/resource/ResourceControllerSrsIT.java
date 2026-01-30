package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class ResourceControllerSrsIT extends ITBase {

  private final ObjectMapper objectMapper = OBJECT_MAPPER;

  @Test
  void createWorkWithInstanceRef_shouldCreateAuthorityFromSrs() throws Exception {
    var existingSrsId = "srs_id_0001";

    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(postResourcePayload(existingSrsId));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var resp = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var creatorResourceId = objectMapper.readTree(resp)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("_creatorReference").get(0)
      .path("id").asLong();

    getGraph(creatorResourceId)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.types[0]", equalTo("PERSON")))
      .andExpect(jsonPath("$.label", equalTo("bValue, aValue, cValue, dValue")))
      .andExpect(jsonPath("$.outgoingEdges", hasSize(2)))
      // Assert LCCN (ID_LCSH, IDENTIFIER, label, and link)
      .andExpect(jsonPath(
        "$.outgoingEdges[?("
          + "@.target.types[?(@ == 'ID_LCSH')] "
          + "&& @.target.types[?(@ == 'IDENTIFIER')] "
          + "&& @.target.label == 'sh85121033' "
          + "&& @.target.doc['http://bibfra.me/vocab/lite/link'][0] == 'http://id.loc.gov/authorities/subjects/sh85121033'"
          + ")]",
        hasSize(1)))
      // Assert LOCAL (ID_LOCAL, IDENTIFIER, label)
      .andExpect(jsonPath(
        "$.outgoingEdges[?("
          + "@.target.types[?(@ == 'ID_LOCAL')] "
          + "&& @.target.types[?(@ == 'IDENTIFIER')] "
          + "&& @.target.label == 'aValue'"
          + ")]",
        hasSize(1)));
  }

  @Test
  void createWorkWithInstanceRef_shouldReturn404_ifRecordNotFoundInSrs() throws Exception {
    var nonExistingSrsId = "non-existing-srs_id-01";

    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(postResourcePayload(nonExistingSrsId));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("errors[0].message",
        equalTo("Source Record not found by srsId: [%s] in Source Record storage".formatted(nonExistingSrsId))))
      .andExpect(jsonPath("errors[0].code", equalTo("not_found")))
      .andExpect(jsonPath("errors[0].parameters", hasSize(4)))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  private ResultActions getGraph(Long resourceId) throws Exception {
    var requestBuilder = get("/linked-data/resource/" + resourceId + "/graph")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    return mockMvc.perform(requestBuilder);
  }

  private String postResourcePayload(String srsId) {
    return """
      {
          "resource": {
              "http://bibfra.me/vocab/lite/Work": {
                  "profileId": 2,
                  "http://bibfra.me/vocab/library/title": [
                      {
                          "http://bibfra.me/vocab/library/Title": {
                              "http://bibfra.me/vocab/library/mainTitle": [ "ResourceControllerSrsIT" ]
                          }
                      }
                  ],
                  "_creatorReference": [ { "srsId": "%s" } ]
              }
          }
      }
      """
      .formatted(srsId);
  }
}
