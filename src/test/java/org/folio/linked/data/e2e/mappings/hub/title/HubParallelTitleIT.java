package org.folio.linked.data.e2e.mappings.hub.title;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class HubParallelTitleIT extends PostResourceIT {

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
                 "http://bibfra.me/vocab/library/ParallelTitle":{
                    "http://bibfra.me/vocab/library/mainTitle":[ "parallel title" ],
                    "http://bibfra.me/vocab/library/partNumber":[ "part number" ],
                    "http://bibfra.me/vocab/library/partName":[ "part name" ],
                    "http://bibfra.me/vocab/library/subTitle":[ "sub title" ],
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
    var parallelTitlePath = titlePath + "[?(@['http://bibfra.me/vocab/library/ParallelTitle'])]"
      + "['http://bibfra.me/vocab/library/ParallelTitle']";

    apiResponse
      .andExpect(jsonPath(mainTitlePath + "['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value(hasItem("TEST: HubParallelTitleIT")))
      .andExpect(jsonPath(parallelTitlePath + "['http://bibfra.me/vocab/library/mainTitle'][0]")
        .value(hasItem("parallel title")))
      .andExpect(jsonPath(parallelTitlePath + "['http://bibfra.me/vocab/library/partNumber'][0]")
        .value(hasItem("part number")))
      .andExpect(jsonPath(parallelTitlePath + "['http://bibfra.me/vocab/library/partName'][0]")
        .value(hasItem("part name")))
      .andExpect(jsonPath(parallelTitlePath + "['http://bibfra.me/vocab/library/subTitle'][0]")
        .value(hasItem("sub title")))
      .andExpect(jsonPath(parallelTitlePath + "['http://bibfra.me/vocab/lite/note'][0]")
        .value(hasItem("note text")))
      .andExpect(jsonPath(parallelTitlePath + "['http://bibfra.me/vocab/lite/date'][0]")
        .value(hasItem("2026")));
  }

  @Override
  protected void validateGraph(Resource hub) {
    var expectedHubLabel = "TEST: HubParallelTitleIT";
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(hub.getLabel()).isEqualTo(expectedHubLabel);
    assertThat(getProperty(hub, "http://bibfra.me/vocab/lite/label")).isEqualTo(expectedHubLabel);

    var titles = getOutgoingResources(hub, "http://bibfra.me/vocab/library/title");
    var mainTitle = getTitleByType(titles, TITLE);
    validateResourceType(mainTitle, "http://bibfra.me/vocab/library/Title");
    assertThat(getProperty(mainTitle, "http://bibfra.me/vocab/library/mainTitle"))
      .isEqualTo("TEST: HubParallelTitleIT");
    assertThat(mainTitle.getLabel()).isEqualTo("TEST: HubParallelTitleIT");

    var parallelTitle = getTitleByType(titles, PARALLEL_TITLE);
    validateResourceType(parallelTitle, "http://bibfra.me/vocab/library/ParallelTitle");
    assertThat(getProperty(parallelTitle, "http://bibfra.me/vocab/library/mainTitle"))
      .isEqualTo("parallel title");
    assertThat(getProperty(parallelTitle, "http://bibfra.me/vocab/library/partNumber"))
      .isEqualTo("part number");
    assertThat(getProperty(parallelTitle, "http://bibfra.me/vocab/library/partName"))
      .isEqualTo("part name");
    assertThat(getProperty(parallelTitle, "http://bibfra.me/vocab/library/subTitle"))
      .isEqualTo("sub title");
    assertThat(getProperty(parallelTitle, "http://bibfra.me/vocab/lite/note"))
      .isEqualTo("note text");
    assertThat(getProperty(parallelTitle, "http://bibfra.me/vocab/lite/date"))
      .isEqualTo("2026");
    assertThat(parallelTitle.getLabel()).isEqualTo("parallel title sub title");
  }

  private Resource getTitleByType(List<Resource> titles, ResourceTypeDictionary type) {
    return titles.stream()
      .filter(title -> title.isOfType(type))
      .findFirst()
      .orElseThrow();
  }
}
