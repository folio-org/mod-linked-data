package org.folio.linked.data.e2e.mappings.work.subject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.FOLIO_OKAPI_URL;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.util.ResourceUtils;
import org.springframework.test.web.servlet.ResultActions;

public class HubAsSubjectIT extends PostResourceIT {
  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Work": {
            "profileId": 2,
            "http://bibfra.me/vocab/library/title": [
              {
                "http://bibfra.me/vocab/library/Title": {
                  "http://bibfra.me/vocab/library/mainTitle": [ "%s" ]
                }
              }
            ],
            "http://bibfra.me/vocab/lite/subject": [
              {
                 "rdfLink": "$RDF_LINK"
              }
            ]
          }
        }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName())
      .replace("$RDF_LINK", getHubUri());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var subjectPath = "$.resource['http://bibfra.me/vocab/lite/Work']['http://bibfra.me/vocab/lite/subject'][0]";
    apiResponse
      .andExpect(jsonPath(subjectPath + "['label']").value("Hub AAP 😊"))
      .andExpect(jsonPath(subjectPath + "['types']", containsInAnyOrder(
        "http://bibfra.me/vocab/lite/Concept",
        "http://bibfra.me/vocab/lite/Hub"
      )))
      .andExpect(jsonPath(subjectPath + "['isPreferred']").value(false));
  }

  @Override
  protected void validateGraph(Resource resource) {
    var concept = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/lite/subject");

    assertThat(concept.getLabel()).isEqualTo("Hub AAP 😊");
    validateResourceType(concept, "http://bibfra.me/vocab/lite/Concept", "http://bibfra.me/vocab/lite/Hub");
    assertThat(ResourceUtils.isPreferred(concept)).isFalse();

    var hub = getFirstOutgoingResource(concept, "http://bibfra.me/vocab/lite/focus");
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(hub.getLabel()).isEqualTo("Hub AAP 😊");
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/label")).isEqualTo("Hub AAP 😊");
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://localhost/some-hub-storage/150986.json");

    var hubTitle = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/library/title");
    validateResourceType(hubTitle, "http://bibfra.me/vocab/library/Title");
    assertThat(getProperty(hubTitle, "http://bibfra.me/vocab/library/mainTitle")).isEqualTo("Hub 150986 mainTitle 汉");
    assertThat(hubTitle.getLabel()).isEqualTo("Hub 150986 mainTitle 汉");
  }

  private String getHubUri() {
    return System.getProperty(FOLIO_OKAPI_URL) + "/some-hub-storage/150986.json";
  }
}
