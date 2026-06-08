package org.folio.linked.data.e2e.mappings;

import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.model.entity.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Extends {@link PostResourceIT} with a PUT round-trip test.
 * Subclasses that need to verify update (PUT) behaviour should extend this class instead of {@link PostResourceIT}.
 */
public abstract class PutResourceIT extends PostResourceIT {

  protected abstract String putPayload();

  protected void validateUpdatedApiResponse(ResultActions apiResponse) {}

  protected void validateUpdatedGraph(Resource resource) {}

  @Test
  void testPutRequest() throws Exception {
    // given – create resource via POST
    var postRequest = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(postPayload());
    var postResponse = mockMvc.perform(postRequest);
    postResponse.andExpect(status().isOk());
    var resourceId = getResourceId(postResponse);

    // when – update via PUT
    var putRequest = put(RESOURCE_URL + "/" + resourceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(putPayload());
    var putResponse = mockMvc.perform(putRequest);

    // then
    putResponse.andExpect(status().isOk());
    validateUpdatedApiResponse(putResponse);
    var updatedResourceId = getResourceId(putResponse);
    var updatedResource = resourceTestService.getResourceById(updatedResourceId, RESOURCE_FETCH_DEPTH);
    validateUpdatedGraph(updatedResource);

    var getRequest = get(RESOURCE_URL + "/" + updatedResourceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var getResponse = mockMvc.perform(getRequest);
    getResponse.andExpect(status().isOk());
    validateUpdatedApiResponse(getResponse);
  }
}
