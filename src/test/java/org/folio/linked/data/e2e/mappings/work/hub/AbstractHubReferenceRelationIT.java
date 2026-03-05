package org.folio.linked.data.e2e.mappings.work.hub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.ResultActions;

abstract class AbstractHubReferenceRelationIT extends PostResourceIT {

  protected abstract String relationUri();

  protected abstract Long hubId();

  @BeforeEach
  void createAuthority() {
    saveHub(hubId());
  }

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
                "_relation": "$RELATION_URI",
                "_hub": {
                  "id": "$HUB_ID",
                  "label": "$HUB_LABEL"
                }
              }
            ]
          }
        }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName())
      .replace("$RELATION_URI", relationUri())
      .replace("$HUB_ID", hubId().toString())
      .replace("$HUB_LABEL", hubLabel());
  }

  @Override
  protected void validateGraph(Resource work) {
    assertThat(work.getOutgoingEdges()).hasSize(2);

    var relationHub = getFirstOutgoingResource(work, relationUri());
    validateResourceType(relationHub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(relationHub.getId()).isEqualTo(hubId());
    assertThat(relationHub.getLabel()).isEqualTo(hubLabel());
    assertThat(getProperty(relationHub, "http://bibfra.me/vocab/lite/label")).isEqualTo(hubLabel());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var workPath = "$.resource['http://bibfra.me/vocab/lite/Work']";
    var relationPath = workPath + "._hubs[?(@._relation=='" + relationUri() + "')]._hub";

    apiResponse
      .andExpect(jsonPath(relationPath + "['label']").value(hubLabel()))
      .andExpect(jsonPath(relationPath + "['types'][0]").value("http://bibfra.me/vocab/lite/Hub"))
      .andExpect(jsonPath(relationPath + "['isPreferred']").value(false));
  }

  @SneakyThrows
  private void saveHub(Long id) {
    var resource = new Resource()
      .addTypes(HUB)
      .setDoc(TEST_JSON_MAPPER.readTree("{\"http://bibfra.me/vocab/lite/label\": [\"%s\"]}".formatted(hubLabel())))
      .setLabel(hubLabel())
      .setIdAndRefreshEdges(id);
    resourceTestService.saveGraph(resource);
  }

  private String hubLabel() {
    return "hub label " + hubId();
  }
}
