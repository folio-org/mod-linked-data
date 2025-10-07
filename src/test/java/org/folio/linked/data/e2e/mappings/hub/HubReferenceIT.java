package org.folio.linked.data.e2e.mappings.hub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class HubReferenceIT extends PostResourceIT {

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
                  "http://bibfra.me/vocab/library/mainTitle": [
                    "%s"
                  ]
                }
              }
            ],
            "_hubs": [
              {
                "_relation": "http://bibfra.me/vocab/lite/expressionOf",
                "_hub": {
                  "http://bibfra.me/vocab/lite/label": [
                    "hub label"
                  ],
                  "http://bibfra.me/vocab/lite/link": [
                    "hub link"
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
  protected void validateGraph(Resource work) {
    var hub = getFirstOutgoingResource(work, "http://bibfra.me/vocab/lite/expressionOf");
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");

    assertThat(hub.getId()).isEqualTo(-1420048856840056963L);
    assertThat(hub.getLabel()).isEqualTo("hub label");
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/label")).isEqualTo("hub label");
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/link")).isEqualTo("hub link");
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var workPath = "$.resource['http://bibfra.me/vocab/lite/Work']";

    apiResponse
      .andExpect(jsonPath(workPath + "['_hubs'][0]['_relation']").value("http://bibfra.me/vocab/lite/expressionOf"))
      .andExpect(jsonPath(workPath + "['_hubs'][0]['_hub']['id']").value("-1420048856840056963"))
      .andExpect(jsonPath(workPath + "['_hubs'][0]['_hub']['http://bibfra.me/vocab/lite/label'][0]").value("hub label"))
      .andExpect(jsonPath(workPath + "['_hubs'][0]['_hub']['http://bibfra.me/vocab/lite/link'][0]").value("hub link"));
  }
}
