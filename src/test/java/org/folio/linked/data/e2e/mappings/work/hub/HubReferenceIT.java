package org.folio.linked.data.e2e.mappings.work.hub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.test.TestUtil.FOLIO_OKAPI_URL;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.ResultActions;

class HubReferenceIT extends PostResourceIT {

  public static final Long EXPRESSION_OF_HUB_ID = 100L;

  @BeforeEach
  void createAuthority() {
    saveHub(EXPRESSION_OF_HUB_ID);
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
                "_relation": "http://bibfra.me/vocab/lite/expressionOf",
                "_hub": {
                  "id": "$EXPRESSION_OF_HUB_ID",
                  "label": "hub label 1"
                }
              },
              {
                "_relation": "http://bibfra.me/vocab/relation/relatedWork",
                "_hub": {
                  "label": "hub label 2",
                  "rdfLink": "$RDF_LINK"
                }
              }
            ]
          }
        }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName())
      .replace("$EXPRESSION_OF_HUB_ID", EXPRESSION_OF_HUB_ID.toString())
      .replace("$RDF_LINK", getHubUri());
  }

  @Override
  protected void validateGraph(Resource work) {
    assertThat(work.getOutgoingEdges()).hasSize(3);

    var expressionOfHub = getFirstOutgoingResource(work, "http://bibfra.me/vocab/lite/expressionOf");
    validateResourceType(expressionOfHub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(expressionOfHub.getId()).isEqualTo(EXPRESSION_OF_HUB_ID);
    assertThat(expressionOfHub.getLabel()).isEqualTo("hub label " + EXPRESSION_OF_HUB_ID);
    assertThat(getProperty(expressionOfHub, "http://bibfra.me/vocab/lite/label")).isEqualTo("hub label " + EXPRESSION_OF_HUB_ID);

    var relatedWorkHub = getFirstOutgoingResource(work, "http://bibfra.me/vocab/relation/relatedWork");
    validateResourceType(relatedWorkHub, "http://bibfra.me/vocab/lite/Hub");
    assertThat(relatedWorkHub.getId()).isEqualTo(2624623618421380585L);
    assertThat(relatedWorkHub.getLabel()).isEqualTo("Hub AAP 😊");
    assertThat(getProperty(relatedWorkHub, "http://bibfra.me/vocab/lite/label")).isEqualTo("Hub AAP 😊");
    assertThat(getProperty(relatedWorkHub, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://localhost/some-hub-storage/150986.json");

    // verify relatedWork hub's title
    var relatedWorkHubTitle = getFirstOutgoingResource(relatedWorkHub, "http://bibfra.me/vocab/library/title");
    validateResourceType(relatedWorkHubTitle, "http://bibfra.me/vocab/library/Title");
    assertThat(getProperty(relatedWorkHubTitle, "http://bibfra.me/vocab/library/mainTitle")).isEqualTo("Hub 150986 mainTitle 汉");
    assertThat(relatedWorkHubTitle.getLabel()).isEqualTo("Hub 150986 mainTitle 汉");
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var workPath = "$.resource['http://bibfra.me/vocab/lite/Work']";
    var relatedWorkPath = workPath + "._hubs[?(@._relation=='http://bibfra.me/vocab/relation/relatedWork')]._hub";
    var expressionOfPath = workPath + "._hubs[?(@._relation=='http://bibfra.me/vocab/lite/expressionOf')]._hub";

    apiResponse
      .andExpect(jsonPath(expressionOfPath + "['label']").value("hub label " + EXPRESSION_OF_HUB_ID))
      .andExpect(jsonPath(expressionOfPath + "['types'][0]").value("http://bibfra.me/vocab/lite/Hub"))
      .andExpect(jsonPath(expressionOfPath + "['isPreferred']").value(false))
      .andExpect(jsonPath(relatedWorkPath + "['label']").value("Hub AAP 😊"))
      .andExpect(jsonPath(relatedWorkPath + "['rdfLink']")
        .value("http://localhost/some-hub-storage/150986.json"))
      .andExpect(jsonPath(relatedWorkPath + "['types'][0]").value("http://bibfra.me/vocab/lite/Hub"))
      .andExpect(jsonPath(relatedWorkPath + "['isPreferred']").value(false));
  }

  @SneakyThrows
  private void saveHub(Long id) {
    var hubLabel = "hub label " + id;
    var resource = new Resource()
      .addTypes(HUB)
      .setDoc(TEST_JSON_MAPPER.readTree("{\"http://bibfra.me/vocab/lite/label\": [\"%s\"]}".formatted(hubLabel)))
      .setLabel(hubLabel)
      .setIdAndRefreshEdges(id);
    resourceTestService.saveGraph(resource);
  }

  private String getHubUri() {
    return System.getProperty(FOLIO_OKAPI_URL) + "/some-hub-storage/150986.json";
  }
}
