package org.folio.linked.data.e2e.mappings;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getWork;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public abstract class PostResourceIT extends ITBase {

  private static final int RESOURCE_FETCH_DEPTH = 4;
  private static final String RESOURCE_URL = "/linked-data/resource";

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

  @SneakyThrows
  private String getResourceId(ResultActions postResponse) {
    var postResponseContent = postResponse.andReturn().getResponse().getContentAsString();
    var resourceNode = TEST_JSON_MAPPER.readTree(postResponseContent).path("resource");

    return Stream.of(
        "http://bibfra.me/vocab/lite/Instance",
        "http://bibfra.me/vocab/lite/Work",
        "http://bibfra.me/vocab/lite/Hub"
      )
      .filter(resourceNode::has)
      .findFirst()
      .map(key -> resourceNode.path(key).path("id").asString())
      .orElseThrow(() -> new RuntimeException("No Instance, Work, or Hub node found in response"));
  }

  protected String getProperty(Resource resource, String property) {
    return getProperties(resource, property).stream()
      .findFirst()
      .orElseThrow();
  }

  protected Set<String> getProperties(Resource resource, String property) {
    return stream(resource.getDoc().get(property).spliterator(), false)
      .map(JsonNode::asString)
      .collect(toSet());
  }

  protected Resource getFirstOutgoingResource(Resource instance, String url) {
    return getOutgoingResources(instance, url).getFirst();
  }

  protected List<Resource> getOutgoingResources(Resource resource, String predicate) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(predicate))
      .map(ResourceEdge::getTarget)
      .toList();
  }

  protected void validateResourceType(Resource resource, String... expectedTypes) {
    var actualTypes = resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .collect(toSet());

    assertThat(actualTypes).isEqualTo(Set.of(expectedTypes));
  }
}
