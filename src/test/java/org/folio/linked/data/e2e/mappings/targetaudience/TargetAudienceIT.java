package org.folio.linked.data.e2e.mappings.targetaudience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class TargetAudienceIT extends PostResourceIT {
  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Work":{
               "profileId": 2,
               "http://bibfra.me/vocab/library/title":[
                  {
                     "http://bibfra.me/vocab/library/Title":{
                        "http://bibfra.me/vocab/library/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/library/targetAudience":[
                  {
                     "http://bibfra.me/vocab/library/term":[ "Primary" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/maudience/pri" ]
                  }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  protected void validateGraph(Resource work) {
    final var expectedId = -5142627795633505421L;
    final var expectedCategorySetId = 5919470580343311333L;
    final var targetAudience = getFirstOutgoingResource(work, "http://bibfra.me/vocab/library/targetAudience");
    assertThat(targetAudience.getId()).isEqualTo(expectedId);
    validateResourceType(targetAudience, "http://bibfra.me/vocab/lite/Category");
    assertThat(getProperty(targetAudience, "http://bibfra.me/vocab/library/term")).isEqualTo("Primary");
    assertThat(getProperty(targetAudience, "http://bibfra.me/vocab/library/code")).isEqualTo("b");
    assertThat(getProperty(targetAudience, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/maudience/pri");
    assertThat(targetAudience.getLabel()).isEqualTo("Primary");

    var categorySet = getFirstOutgoingResource(targetAudience, "http://bibfra.me/vocab/lite/isDefinedBy");
    validateResourceType(categorySet, "http://bibfra.me/vocab/lite/CategorySet");
    assertThat(categorySet.getId()).isEqualTo(expectedCategorySetId);
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label")).isEqualTo("Target audience");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/maudience");
    assertThat(categorySet.getLabel()).isEqualTo("Target audience");
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var audiencePath = "$.resource['http://bibfra.me/vocab/lite/Work']['http://bibfra.me/vocab/library/targetAudience']";
    apiResponse
      .andExpect(jsonPath(audiencePath + "[0]['http://bibfra.me/vocab/library/term'][0]").value("Primary"))
      .andExpect(jsonPath(audiencePath + "[0]['http://bibfra.me/vocab/library/code'][0]").value("b"))
      .andExpect(jsonPath(audiencePath + "[0]['http://bibfra.me/vocab/lite/link'][0]")
        .value("http://id.loc.gov/vocabulary/maudience/pri"));
  }
}
