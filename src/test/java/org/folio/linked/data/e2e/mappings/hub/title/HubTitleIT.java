package org.folio.linked.data.e2e.mappings.hub.title;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class HubTitleIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Hub": {
            "profileId": 3,
            "http://bibfra.me/vocab/library/title": [
              {
                "http://bibfra.me/vocab/library/Title": {
                  "http://bibfra.me/vocab/library/mainTitle": [
                    "%s"
                  ]
                }
              }
            ]
          }
        }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var hubPath = "$.resource['http://bibfra.me/vocab/lite/Hub']";

    apiResponse
      .andExpect(jsonPath(hubPath + "['id']").value("-6603120915378868727"))
      .andExpect(jsonPath(hubPath + "['http://bibfra.me/vocab/library/title'][0]"
        + "['http://bibfra.me/vocab/library/Title']['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value("TEST: HubTitleIT"));
  }

  @Override
  protected void validateGraph(Resource hub) {
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(hub.getLabel()).isEqualTo("TEST: HubTitleIT");
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/label")).isEqualTo("TEST: HubTitleIT");

    var title = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/library/title");
    validateResourceType(title, "http://bibfra.me/vocab/library/Title");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/mainTitle")).isEqualTo("TEST: HubTitleIT");
    assertThat(title.getLabel()).isEqualTo("TEST: HubTitleIT");
  }
}
