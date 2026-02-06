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
                  "http://bibfra.me/vocab/library/mainTitle": [ "%s" ],
                  "http://bibfra.me/vocab/library/partNumber": [ "part number" ],
                  "http://bibfra.me/vocab/library/partName": [ "part name" ],
                  "http://bibfra.me/vocab/bflc/nonSortNum": [ "non sort num" ],
                  "http://bibfra.me/vocab/library/subTitle": [ "sub title" ]
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
    var titlePath = hubPath + "['http://bibfra.me/vocab/library/title'][0]['http://bibfra.me/vocab/library/Title']";

    apiResponse
      .andExpect(jsonPath(hubPath + "['id']").value("-3606019457490474046"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/mainTitle'][0]").value("TEST: HubTitleIT"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/partNumber'][0]").value("part number"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/partName'][0]").value("part name"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/bflc/nonSortNum'][0]").value("non sort num"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/subTitle'][0]").value("sub title"));
  }

  @Override
  protected void validateGraph(Resource hub) {
    var expectedHubLabel = "TEST: HubTitleIT sub title";
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(hub.getLabel()).isEqualTo(expectedHubLabel);
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/label")).isEqualTo(expectedHubLabel);

    var title = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/library/title");
    validateResourceType(title, "http://bibfra.me/vocab/library/Title");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/mainTitle")).isEqualTo("TEST: HubTitleIT");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/partNumber")).isEqualTo("part number");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/partName")).isEqualTo("part name");
    assertThat(getProperty(title, "http://bibfra.me/vocab/bflc/nonSortNum")).isEqualTo("non sort num");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/subTitle")).isEqualTo("sub title");
    assertThat(title.getLabel()).isEqualTo(expectedHubLabel);
  }
}
