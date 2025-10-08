package org.folio.linked.data.e2e.mappings.hub.creator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

class HubCreatorIT extends PostResourceIT {

  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void createAuthority() {
    saveResource(1L, "UN General assembly meeting", ResourceTypeDictionary.MEETING, "srsId1");
    saveResource(2L, "United States", ResourceTypeDictionary.JURISDICTION, "srsId2");
  }

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
            "_creatorReference":[
              {
                "srsId":"srsId1",
                "roles":[ "http://bibfra.me/vocab/relation/author", "http://bibfra.me/vocab/relation/editor" ]
              }
            ],
            "_contributorReference":[
              {
                "id":"2",
                "roles":[ "http://bibfra.me/vocab/relation/sponsoringbody" ]
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

    apiResponse
      .andExpect(jsonPath(hubPath + "['_creatorReference'][0]['id']").value("1"))
      .andExpect(jsonPath(hubPath + "['_creatorReference'][0]['label']").value("UN General assembly meeting"))
      .andExpect(jsonPath(hubPath + "['_creatorReference'][0]['type']")
        .value("http://bibfra.me/vocab/lite/Meeting"))
      .andExpect(jsonPath(hubPath + "['_creatorReference'][0]['roles']", containsInAnyOrder(
        "http://bibfra.me/vocab/relation/author", "http://bibfra.me/vocab/relation/editor")))
      .andExpect(jsonPath(hubPath + "['_contributorReference'][0]['id']").value("2"))
      .andExpect(jsonPath(hubPath + "['_contributorReference'][0]['label']").value("United States"))
      .andExpect(jsonPath(hubPath + "['_contributorReference'][0]['type']")
        .value("http://bibfra.me/vocab/lite/Jurisdiction"))
      .andExpect(jsonPath(hubPath + "['_contributorReference'][0]['roles']", containsInAnyOrder(
        "http://bibfra.me/vocab/relation/sponsoringbody")));
  }

  @Override
  protected void validateGraph(Resource hub) {
    validateResourceType(hub, "http://bibfra.me/vocab/lite/Hub");
    var creator = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/lite/creator");
    validateResourceType(creator, "http://bibfra.me/vocab/lite/Meeting");
    assertThat(creator.getId()).isEqualTo(1L);
    assertThat(creator.getLabel()).isEqualTo("UN General assembly meeting");

    var author = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/relation/author");
    var editor = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/relation/editor");
    assertThat(creator).isEqualTo(author).isEqualTo(editor);

    var contributor = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/lite/contributor");
    validateResourceType(contributor, "http://bibfra.me/vocab/lite/Jurisdiction");
    assertThat(contributor.getId()).isEqualTo(2L);
    assertThat(contributor.getLabel()).isEqualTo("United States");

    var sponsoringBody = getFirstOutgoingResource(hub, "http://bibfra.me/vocab/relation/sponsoringbody");
    assertThat(contributor).isEqualTo(sponsoringBody);
  }

  @SneakyThrows
  private void saveResource(Long id, String label, ResourceTypeDictionary type, String srsId) {
    var resource = new Resource()
      .addType(new ResourceTypeEntity().setHash(type.getHash()).setUri(type.getUri()))
      .setDoc(objectMapper.readTree("{}"))
      .setLabel(label)
      .setId(id);
    resource.setFolioMetadata(new FolioMetadata(resource).setSrsId(srsId));
    resourceTestService.saveGraph(resource);
  }
}
