package org.folio.linked.data.e2e.mappings.targetaudience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.test.web.servlet.ResultActions;

class TargetAudienceIT extends PostResourceIT {
  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Work":{
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/marc/targetAudience":[
                  {
                     "http://bibfra.me/vocab/marc/term":[ "Primary" ],
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
    var expectedId = 7128309939039775870L;
    var expectedCategorySetId = 5919470580343311333L;
    var targetAudience = getTarget(work, "http://bibfra.me/vocab/marc/targetAudience");
    assertThat(targetAudience.getId()).isEqualTo(expectedId);
    assertThat(getProperty(targetAudience, "http://bibfra.me/vocab/marc/term")).isEqualTo("Primary");
    assertThat(getProperty(targetAudience, "http://bibfra.me/vocab/marc/code")).isEqualTo("b");
    assertThat(getProperty(targetAudience, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/maudience/pri");
    assertThat(targetAudience.getLabel()).isEqualTo("Primary");

    var categorySet = getTarget(targetAudience, "http://bibfra.me/vocab/lite/isDefinedBy");
    assertThat(categorySet.getId()).isEqualTo(expectedCategorySetId);
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label")).isEqualTo("Target audience");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/maudience");
    assertThat(categorySet.getLabel()).isEqualTo("Target audience");
  }

  @Override
  protected void validateApiResponse(ResultActions apiResponse) throws Exception {
    var audiencePath = "$.resource['http://bibfra.me/vocab/lite/Work']['http://bibfra.me/vocab/marc/targetAudience']";
    apiResponse
      .andExpect(status().isOk())
      .andExpect(jsonPath(audiencePath + "[0]['http://bibfra.me/vocab/marc/term'][0]").value("Primary"))
      .andExpect(jsonPath(audiencePath + "[0]['http://bibfra.me/vocab/marc/code'][0]").value("b"))
      .andExpect(jsonPath(audiencePath + "[0]['http://bibfra.me/vocab/lite/link'][0]")
        .value("http://id.loc.gov/vocabulary/maudience/pri"));
  }

  private String getProperty(Resource resource, String property) {
    return resource.getDoc().get(property).get(0).asText();
  }

  private Resource getTarget(Resource resource, String predicate) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(predicate))
      .map(ResourceEdge::getTarget)
      .findFirst().orElseThrow();
  }
}
