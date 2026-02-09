package org.folio.linked.data.e2e.mappings.hub.title;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class HubVariantTitleIT extends PostResourceIT {

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
              },
              {
                 "http://bibfra.me/vocab/library/VariantTitle":{
                    "http://bibfra.me/vocab/library/mainTitle":[ "variant title" ],
                    "http://bibfra.me/vocab/library/partNumber":[ "part number" ],
                    "http://bibfra.me/vocab/library/partName":[ "part name" ],
                    "http://bibfra.me/vocab/library/subTitle":[ "sub title" ],
                    "http://bibfra.me/vocab/library/variantType":[ "variant type" ],
                    "http://bibfra.me/vocab/lite/note":[ "note text" ],
                    "http://bibfra.me/vocab/lite/date":[ "2026" ]
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
    var titlePath = hubPath + "['http://bibfra.me/vocab/library/title']";
    var mainTitlePath = titlePath + "[?(@['http://bibfra.me/vocab/library/Title'])]"
      + "['http://bibfra.me/vocab/library/Title']";
    var variantTitlePath = titlePath + "[?(@['http://bibfra.me/vocab/library/VariantTitle'])]"
      + "['http://bibfra.me/vocab/library/VariantTitle']";

    apiResponse
      .andExpect(jsonPath(mainTitlePath + "['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value(hasItem("TEST: HubVariantTitleIT")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value(hasItem("variant title")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/library/partNumber'][0]")
        .value(hasItem("part number")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/library/partName'][0]")
        .value(hasItem("part name")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/library/subTitle'][0]")
        .value(hasItem("sub title")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/library/variantType'][0]")
        .value(hasItem("variant type")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/lite/note'][0]")
        .value(hasItem("note text")))
      .andExpect(jsonPath(variantTitlePath + "['http://bibfra.me/vocab/lite/date'][0]")
        .value(hasItem("2026")));
  }

  @Override
  protected void validateGraph(Resource hub) {
    var titles = getOutgoingResources(hub, "http://bibfra.me/vocab/library/title");
    var variantTitle = getVariantTitle(titles);

    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/library/mainTitle"))
      .isEqualTo("variant title");
    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/library/partNumber"))
      .isEqualTo("part number");
    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/library/partName"))
      .isEqualTo("part name");
    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/library/subTitle"))
      .isEqualTo("sub title");
    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/library/variantType"))
      .isEqualTo("variant type");
    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/lite/note"))
      .isEqualTo("note text");
    assertThat(getProperty(variantTitle, "http://bibfra.me/vocab/lite/date"))
      .isEqualTo("2026");
    assertThat(variantTitle.getLabel()).isEqualTo("variant title sub title");
  }

  private Resource getVariantTitle(List<Resource> titles) {
    return titles.stream()
      .filter(title -> title.isOfType(ResourceTypeDictionary.VARIANT_TITLE))
      .findFirst()
      .orElseThrow();
  }
}
