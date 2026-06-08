package org.folio.linked.data.e2e.mappings;

import static org.folio.linked.data.test.MonographTestUtil.getWork;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getResourceId;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public abstract class PostResourceIT extends ITBase {

  protected static final String RESOURCE_URL = "/linked-data/resource";
  protected static final int RESOURCE_FETCH_DEPTH = 4;

  protected Long savedWorkId;

  @BeforeEach
  void setupWork() {
    savedWorkId = null;
  }

  protected void createAndSaveSampleWork() {
    savedWorkId = resourceTestService.saveGraph(getWork(getClass().getSimpleName(), hashService)).getId();
  }

  protected String workReferenceJson() {
    return savedWorkId != null
      ? ",%n              \"_workReference\": [ { \"id\": \"%s\" } ]".formatted(savedWorkId)
      : "";
  }

  protected abstract String postPayload();

  protected abstract void validateApiResponse(ResultActions apiResponse);

  protected abstract void validateGraph(Resource resource);

  @Test
  void testPostRequest() throws Exception {
    // given
    var requestPayload = postPayload();

    var postRequest = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(requestPayload);

    // when
    var postResponse = mockMvc.perform(postRequest);

    // then
    postResponse.andExpect(status().isOk());
    validateApiResponse(postResponse);
    var resourceId = getResourceId(postResponse);
    var resource = resourceTestService.getResourceById(resourceId, RESOURCE_FETCH_DEPTH);
    validateGraph(resource);

    var getRequest = get(RESOURCE_URL + "/" + resourceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var getResponse = mockMvc.perform(getRequest);
    getResponse.andExpect(status().isOk());
    validateApiResponse(getResponse);
  }
}
