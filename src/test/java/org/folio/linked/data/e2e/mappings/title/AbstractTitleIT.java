package org.folio.linked.data.e2e.mappings.title;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

public abstract class AbstractTitleIT extends PostResourceIT {

  protected abstract String resourceUri();

  protected abstract String resourceId();

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "%s": {
            "profileId": 2,
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
      .formatted(resourceUri(), expectedMainTitle());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse.andExpect(jsonPath(rootPath() + "['id']").value(resourceId()));
    var titlePath = rootPath() + "['http://bibfra.me/vocab/library/title'][0]['http://bibfra.me/vocab/library/Title']";

    apiResponse
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/mainTitle'][0]").value(expectedMainTitle()))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/partNumber'][0]").value("part number"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/partName'][0]").value("part name"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/bflc/nonSortNum'][0]").value("non sort num"))
      .andExpect(jsonPath(titlePath + "['http://bibfra.me/vocab/library/subTitle'][0]").value("sub title"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    assertThat(resource.getLabel()).isEqualTo(expectedResourceLabel());
    validateRootResourceType(resource);
    var title = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/library/title");
    validateResourceType(title, "http://bibfra.me/vocab/library/Title");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/mainTitle")).isEqualTo(expectedMainTitle());
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/partNumber")).isEqualTo("part number");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/partName")).isEqualTo("part name");
    assertThat(getProperty(title, "http://bibfra.me/vocab/bflc/nonSortNum")).isEqualTo("non sort num");
    assertThat(getProperty(title, "http://bibfra.me/vocab/library/subTitle")).isEqualTo("sub title");
    assertThat(title.getLabel()).isEqualTo(expectedTitleLabel());
  }

  private String expectedMainTitle() {
    return "TEST: " + this.getClass().getSimpleName();
  }

  protected String expectedResourceLabel() {
    return expectedMainTitle() + " sub title";
  }

  protected String expectedTitleLabel() {
    return expectedMainTitle() + " sub title part number part name";
  }

  private String rootPath() {
    return "$.resource['" + resourceUri() + "']";
  }

  protected void validateRootResourceType(Resource resource) {
    validateResourceType(resource, resourceUri());
  }
}
