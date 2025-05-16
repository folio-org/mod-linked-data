package org.folio.linked.data.e2e.mappings;

import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public abstract class PostResourceIT {
  static final String RESOURCE_URL = "/linked-data/resource";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private ResourceTestService resourceService;
  @Autowired
  private ObjectMapper objectMapper;

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
    validateApiResponse(postResponse);
    var instanceId = getResourceId(postResponse);
    var instance = resourceService.getResourceById(instanceId, 3);
    validateGraph(instance);

    var getRequest = get(RESOURCE_URL + "/" + instanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var getResponse = mockMvc.perform(getRequest);
    validateApiResponse(getResponse);
  }

  private String getResourceId(ResultActions postResponse) {
    try {
      var postResponseContent = postResponse.andReturn().getResponse().getContentAsString();
      var jsonNode = objectMapper.readTree(postResponseContent);

      var instanceOrWorkNode = jsonNode.path("resource")
        .path("http://bibfra.me/vocab/lite/Instance").isMissingNode()
        ? jsonNode.path("resource").path("http://bibfra.me/vocab/lite/Work")
        : jsonNode.path("resource").path("http://bibfra.me/vocab/lite/Instance");

      return instanceOrWorkNode.path("id").asText();

    } catch (JsonProcessingException | UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract String postPayload();

  protected void validateApiResponse(ResultActions apiResponse) throws Exception {
  }

  protected void validateGraph(Resource resource){
  }
}
