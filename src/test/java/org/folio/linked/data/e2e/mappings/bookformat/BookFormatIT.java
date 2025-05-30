package org.folio.linked.data.e2e.mappings.bookformat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
class BookFormatIT extends ITBase {
  static final String RESOURCE_URL = "/linked-data/resource";

  @Autowired
  protected ResourceTestService resourceService;

  @Test
  void postResource_shouldCreateBookFormat() throws Exception {
    // given
    var requestPayload = postInstanceApiRequest();
    var expectedInstanceId = -7766153465587539469L;

    var postRequest = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(requestPayload);

    // when
    var postResponse = mockMvc.perform(postRequest);

    // then
    validateApiResponse(postResponse);

    validateGraph(expectedInstanceId);

    var getRequest = get(RESOURCE_URL + "/" + expectedInstanceId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var getResponse = mockMvc.perform(getRequest);
    validateApiResponse(getResponse);
  }

  private String postInstanceApiRequest() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[ "title" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/marc/bookFormat":[
                  {
                     "http://bibfra.me/vocab/marc/term":[ "128mo" ],
                     "http://bibfra.me/vocab/lite/link": ["http://id.loc.gov/vocabulary/bookformat/128mo"]
                  }, {
                     "http://bibfra.me/vocab/marc/term":[ "non-standard-format" ]
                  }
               ]
            }
         }
      }""";
  }

  private void validateApiResponse(ResultActions apiResponse) throws Exception {
    var bookFormatPath = "$.resource['http://bibfra.me/vocab/lite/Instance']['http://bibfra.me/vocab/marc/bookFormat']";
    apiResponse
      .andExpect(status().isOk())
      .andExpect(
        jsonPath(bookFormatPath + "[0]['http://bibfra.me/vocab/marc/term'][0]")
          .value("non-standard-format"))
      .andExpect(jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/marc/term'][0]")
        .value("128mo"))
      .andExpect(
        jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/marc/code'][0]")
          .value("128mo"))
      .andExpect(
        jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/lite/link'][0]")
          .value("http://id.loc.gov/vocabulary/bookformat/128mo"));
  }

  private void validateGraph(long instanceId) {
    var expectedBookFormatId = 1710735011707999802L;
    var expectedCategorySetId = -5037749211942465056L;
    var instance = resourceService.getResourceById(instanceId + "", 3);
    assertThat(instance.getId()).isEqualTo(instanceId);
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/bookFormat"))
      .isEqualTo("non-standard-format");

    var bookFormat = getTarget(instance, "http://bibfra.me/vocab/marc/bookFormat");
    assertThat(bookFormat.getId()).isEqualTo(expectedBookFormatId);
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/marc/term")).isEqualTo("128mo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/marc/code")).isEqualTo("128mo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat/128mo");
    assertThat(bookFormat.getLabel()).isEqualTo("128mo");

    var categorySet = getTarget(bookFormat, "http://bibfra.me/vocab/lite/isDefinedBy");
    assertThat(categorySet.getId()).isEqualTo(expectedCategorySetId);
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label")).isEqualTo("Book Format");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat");
    assertThat(categorySet.getLabel()).isEqualTo("Book Format");
  }

  private String getProperty(Resource resource, String property) {
    return resource.getDoc().get(property).get(0).asText();
  }

  private Resource getTarget(Resource resource, String predicate) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(predicate))
      .map(ResourceEdge::getTarget)
      .findFirst().orElseThrow();
  }
}
