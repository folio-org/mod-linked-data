package org.folio.linked.data.e2e.mappings.hub.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
              },
              {
                "http://bibfra.me/vocab/library/term": ["non-standard language"]
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
    var languagePath = "$.resource['http://bibfra.me/vocab/lite/Hub']['http://bibfra.me/vocab/lite/language']";
    var englishPath = languagePath + "[?(@['http://bibfra.me/vocab/library/term'][0]=='English')]";
    var expectedLanguageId = -878606130574011566L;
    apiResponse
      .andExpect(jsonPath(languagePath + "[*]['http://bibfra.me/vocab/library/term'][0]").value(hasItem("English")))
      .andExpect(jsonPath(englishPath + ".id").value(hasItem(String.valueOf(expectedLanguageId))))
      .andExpect(jsonPath(englishPath + "['http://bibfra.me/vocab/library/code'][0]").value(hasItem("eng")))
      .andExpect(jsonPath(englishPath + "['http://bibfra.me/vocab/lite/link'][0]").value(hasItem("http://id.loc.gov/vocabulary/languages/eng")))
      .andExpect(jsonPath(languagePath + "[*]['http://bibfra.me/vocab/library/term'][0]").value(hasItem("non-standard language")));
  }

  @Override
  protected void validateGraph(Resource hub) {
    var expectedHubLabel = "TEST: HubLanguageIT. eng";
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(hub.getLabel()).isEqualTo(expectedHubLabel);
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/label")).isEqualTo(expectedHubLabel);
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/language")).isEqualTo("non-standard language");

    var language = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/lite/language");
    validateResourceType(language, "http://bibfra.me/vocab/lite/LanguageCategory");
    assertThat(getProperty(language, "http://bibfra.me/vocab/lite/link")).isEqualTo("http://id.loc.gov/vocabulary/languages/eng");
    assertThat(getProperty(language, "http://bibfra.me/vocab/library/term")).isEqualTo("English");
    assertThat(getProperty(language, "http://bibfra.me/vocab/library/code")).isEqualTo("eng");
    assertThat(language.getLabel()).isEqualTo("eng");
  }
}
