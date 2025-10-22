package org.folio.linked.data.e2e.mappings.work.hub;

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
                  "http://bibfra.me/vocab/library/mainTitle": [ "%s" ]
                }
              }
            ],
            "_hubs": [
              {
                "_relation": "http://bibfra.me/vocab/lite/expressionOf",
                "_hub": {
                  "http://bibfra.me/vocab/lite/label": [ "hub label 1" ],
                  "http://bibfra.me/vocab/lite/link": [ "hub link 1" ]
                }
              },
              {
                "_relation": "http://bibfra.me/vocab/relation/relatedTo",
                "_hub": {
                  "http://bibfra.me/vocab/lite/label": [ "hub label 2" ],
                  "http://bibfra.me/vocab/lite/link": [ "hub link 2" ]
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
    assertThat(work.getOutgoingEdges()).hasSize(3);

    var expressionOfHub = getFirstOutgoingResource(work, "http://bibfra.me/vocab/lite/expressionOf");
    validateResourceType(expressionOfHub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(expressionOfHub.getId()).isEqualTo(-2469667353235297183L);
    assertThat(expressionOfHub.getLabel()).isEqualTo("hub label 1");
    assertThat(getProperty(expressionOfHub, "http://bibfra.me/vocab/lite/label")).isEqualTo("hub label 1");
    assertThat(getProperty(expressionOfHub, "http://bibfra.me/vocab/lite/link")).isEqualTo("hub link 1");

    var relatedToHub = getFirstOutgoingResource(work, "http://bibfra.me/vocab/relation/relatedTo");
    validateResourceType(relatedToHub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(relatedToHub.getId()).isEqualTo(8331756295276184518L);
    assertThat(relatedToHub.getLabel()).isEqualTo("hub label 2");
    assertThat(getProperty(relatedToHub, "http://bibfra.me/vocab/lite/label")).isEqualTo("hub label 2");
    assertThat(getProperty(relatedToHub, "http://bibfra.me/vocab/lite/link")).isEqualTo("hub link 2");
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var workPath = "$.resource['http://bibfra.me/vocab/lite/Work']";
    var relatedToPath = workPath + "._hubs[?(@._relation=='http://bibfra.me/vocab/relation/relatedTo')]._hub";
    var expressionOfPath = workPath + "._hubs[?(@._relation=='http://bibfra.me/vocab/lite/expressionOf')]._hub";

    apiResponse
      .andExpect(jsonPath(expressionOfPath + "['http://bibfra.me/vocab/lite/label'][0]").value("hub label 1"))
      .andExpect(jsonPath(expressionOfPath + "['http://bibfra.me/vocab/lite/link'][0]").value("hub link 1"))
      .andExpect(jsonPath(relatedToPath + "['http://bibfra.me/vocab/lite/label'][0]").value("hub label 2"))
      .andExpect(jsonPath(relatedToPath + "['http://bibfra.me/vocab/lite/link'][0]").value("hub link 2"));
  }
}
