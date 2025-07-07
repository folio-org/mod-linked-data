package org.folio.linked.data.e2e.mappings;

import static java.util.stream.StreamSupport.stream;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public abstract class PostResourceIT extends ITBase {
  static final String RESOURCE_URL = "/linked-data/resource";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private ResourceTestService resourceService;
  @Autowired
  private ObjectMapper objectMapper;

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
    var instanceId = getResourceId(postResponse);
    var instance = resourceService.getResourceById(instanceId, 3);
    validateGraph(instance);

    var getRequest = get(RESOURCE_URL + "/" + instanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var getResponse = mockMvc.perform(getRequest);
    getResponse.andExpect(status().isOk());
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

  protected String getProperty(Resource resource, String property) {
    return getProperties(resource, property).stream()
      .findFirst()
      .orElseThrow();
  }

  protected Set<String> getProperties(Resource resource, String property) {
    return stream(resource.getDoc().get(property).spliterator(), false)
      .map(JsonNode::asText)
      .collect(Collectors.toSet());
  }

  protected List<Resource> getOutgoingResources(Resource resource, String predicate) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(predicate))
      .map(ResourceEdge::getTarget)
      .toList();
  }
}
