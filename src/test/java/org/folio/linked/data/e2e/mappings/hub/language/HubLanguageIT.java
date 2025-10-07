package org.folio.linked.data.e2e.mappings.hub.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class HubLanguageIT extends PostResourceIT {

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
            ],
            "http://bibfra.me/vocab/lite/language":[
              {
                "http://bibfra.me/vocab/library/term": ["English"],
                "http://bibfra.me/vocab/lite/link": ["http://id.loc.gov/vocabulary/languages/eng"]
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
    var languagePath = "$.resource['http://bibfra.me/vocab/lite/Hub']['http://bibfra.me/vocab/lite/language'][0]";
    var expectedLanguageId = -878606130574011566L;
    apiResponse
      .andExpect(jsonPath(languagePath + ".id").value(expectedLanguageId))
      .andExpect(jsonPath(languagePath + "['http://bibfra.me/vocab/library/code'][0]").value("eng"))
      .andExpect(jsonPath(languagePath + "['http://bibfra.me/vocab/library/term'][0]").value("English"))
      .andExpect(jsonPath(languagePath + "['http://bibfra.me/vocab/lite/link'][0]")
        .value("http://id.loc.gov/vocabulary/languages/eng"));
  }

  @Override
  protected void validateGraph(Resource hub) {
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");

    var language = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/lite/language");
    validateResourceType(language, "http://bibfra.me/vocab/lite/LanguageCategory");
    assertThat(getProperty(language, "http://bibfra.me/vocab/lite/link")).isEqualTo("http://id.loc.gov/vocabulary/languages/eng");
    assertThat(getProperty(language, "http://bibfra.me/vocab/library/term")).isEqualTo("English");
    assertThat(getProperty(language, "http://bibfra.me/vocab/library/code")).isEqualTo("eng");
    assertThat(language.getLabel()).isEqualTo("eng");
  }
}
