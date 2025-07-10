package org.folio.linked.data.e2e.mappings.partofseries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.test.web.servlet.ResultActions;

class PartOfSeriesIT extends PostResourceIT {
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
               "http://bibfra.me/vocab/relation/isPartOf":[
                  {
                    "http://bibfra.me/vocab/lite/name":[ "Title 1" ],
                    "http://bibfra.me/vocab/marc/volume":[ "Volume 1" ],
                    "http://bibfra.me/vocab/marc/issn":[ "ISSN 1" ]
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
    String path = "$.resource['http://bibfra.me/vocab/lite/Work']['http://bibfra.me/vocab/relation/isPartOf'][0]";
    apiResponse
      .andExpect(status().isOk())
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/lite/name'][0]").value("Title 1"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/marc/volume'][0]").value("Volume 1"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/marc/issn'][0]").value("ISSN 1"));
  }

  @Override
  protected void validateGraph(Resource work) {
    final var expectedWorkSeriesId = 540189686654989479L;
    final var expectedSeriesId = -2005579814488946952L;
    final var expectedIssnId = -8106115103629407198L;
    final var expectedInstanceId = 5340315565831391363L;

    var workSeries = getFirstOutgoingResource(work, "http://bibfra.me/vocab/relation/isPartOf");
    assertThat(workSeries.getId()).isEqualTo(expectedWorkSeriesId);
    validateResourceType(workSeries,
      "http://bibfra.me/vocab/lite/Work", "http://bibfra.me/vocab/lite/Series");
    assertThat(getProperty(workSeries, "http://bibfra.me/vocab/lite/name")).isEqualTo("Title 1");
    assertThat(getProperty(workSeries, "http://bibfra.me/vocab/marc/volume")).isEqualTo("Volume 1");
    assertThat(getProperty(workSeries, "http://bibfra.me/vocab/lite/label"))
      .isEqualTo("Title 1 Volume 1");

    var series = getFirstOutgoingResource(workSeries, "http://bibfra.me/vocab/relation/isPartOf");
    assertThat(series.getId()).isEqualTo(expectedSeriesId);
    validateResourceType(series, "http://bibfra.me/vocab/lite/Series");
    assertThat(getProperty(series, "http://bibfra.me/vocab/lite/name")).isEqualTo("Title 1");
    assertThat(getProperty(series, "http://bibfra.me/vocab/marc/issn")).isEqualTo("ISSN 1");
    assertThat(getProperty(series, "http://bibfra.me/vocab/lite/label")).isEqualTo("Title 1");

    var issn = getFirstOutgoingResource(series, "http://library.link/vocab/map");
    assertThat(issn.getId()).isEqualTo(expectedIssnId);
    validateResourceType(issn,
      "http://bibfra.me/vocab/lite/Identifier", "http://library.link/identifier/ISSN");
    assertThat(getProperty(issn, "http://bibfra.me/vocab/lite/name")).isEqualTo("ISSN 1");
    assertThat(getProperty(issn, "http://bibfra.me/vocab/lite/label")).isEqualTo("ISSN 1");

    var instanceSeries = getFirstIncomingResource(series, "http://bibfra.me/vocab/lite/instantiates");
    assertThat(instanceSeries.getId()).isEqualTo(expectedInstanceId);
    validateResourceType(instanceSeries,
      "http://bibfra.me/vocab/lite/Instance", "http://bibfra.me/vocab/lite/Series");
    assertThat(getProperty(instanceSeries, "http://bibfra.me/vocab/lite/name")).isEqualTo("Title 1");
    assertThat(getProperty(instanceSeries, "http://bibfra.me/vocab/lite/label")).isEqualTo("Title 1");

    var instanceIssn = getFirstOutgoingResource(instanceSeries, "http://library.link/vocab/map");
    assertThat(instanceIssn).isEqualTo(issn);
  }

  private Resource getFirstOutgoingResource(Resource instance, String url) {
    return getOutgoingResources(instance, url).getFirst();
  }

  private Resource getFirstIncomingResource(Resource resource, String predicate) {
    return resource.getIncomingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(predicate))
      .map(ResourceEdge::getSource)
      .findFirst()
      .orElseThrow();
  }
}
