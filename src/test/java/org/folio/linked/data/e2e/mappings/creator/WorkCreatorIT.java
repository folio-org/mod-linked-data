package org.folio.linked.data.e2e.mappings.creator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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

class WorkCreatorIT extends PostResourceIT {
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void createAuthority() {
    saveResource(1L, "John Doe", ResourceTypeDictionary.PERSON, "srsId1");
    saveResource(2L, "John Doe Society", ResourceTypeDictionary.ORGANIZATION, "srsId2");
    saveResource(3L, "John Doe Family", ResourceTypeDictionary.FAMILY, "srsId3");
  }

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
               "_creatorReference":[
                  {
                     "srsId":"srsId1",
                     "roles":[ "http://bibfra.me/vocab/relation/author", "http://bibfra.me/vocab/relation/editor" ]
                  }
               ],
               "_contributorReference":[
                  {
                     "id":"2",
                     "roles":[ "http://bibfra.me/vocab/relation/funder" ]
                  },
                  { "srsId":"srsId3" }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  protected void validateGraph(Resource work) {
    var creator = getFirstOutgoingResource(work, "http://bibfra.me/vocab/lite/creator");
    validateResourceType(creator, "http://bibfra.me/vocab/lite/Person");
    assertThat(creator.getId()).isEqualTo(1L);
    assertThat(creator.getLabel()).isEqualTo("John Doe");

    var author = getFirstOutgoingResource(work, "http://bibfra.me/vocab/relation/author");
    var editor = getFirstOutgoingResource(work, "http://bibfra.me/vocab/relation/editor");
    assertThat(creator).isEqualTo(author).isEqualTo(editor);

    var contributors = getOutgoingResources(work, "http://bibfra.me/vocab/lite/contributor");
    assertThat(contributors.size()).isEqualTo(2);
    assertThat(contributors)
      .extracting(Resource::getId, Resource::getLabel)
      .containsExactlyInAnyOrder(
        tuple(2L, "John Doe Society"),
        tuple(3L, "John Doe Family")
      );
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var workPath = "$.resource['http://bibfra.me/vocab/lite/Work']";

    apiResponse
      .andExpect(jsonPath(workPath + "['_creatorReference'][0]['id']").value("1"))
      .andExpect(jsonPath(workPath + "['_creatorReference'][0]['label']").value("John Doe"))
      .andExpect(jsonPath(workPath + "['_creatorReference'][0]['type']")
        .value("http://bibfra.me/vocab/lite/Person"))
      .andExpect(jsonPath(workPath + "['_creatorReference'][0]['roles']", containsInAnyOrder(
        "http://bibfra.me/vocab/relation/author", "http://bibfra.me/vocab/relation/editor")));

    apiResponse
      .andExpect(jsonPath(workPath + "['_contributorReference'][0]['id']").value("2"))
      .andExpect(jsonPath(workPath + "['_contributorReference'][0]['label']")
        .value("John Doe Society"))
      .andExpect(jsonPath(workPath + "['_contributorReference'][0]['type']")
        .value("http://bibfra.me/vocab/lite/Organization"))
      .andExpect(jsonPath(workPath + "['_contributorReference'][0]['roles'][0]")
        .value("http://bibfra.me/vocab/relation/funder"));

    apiResponse
      .andExpect(jsonPath(workPath + "['_contributorReference'][1]['id']").value("3"))
      .andExpect(jsonPath(workPath + "['_contributorReference'][1]['label']")
        .value("John Doe Family"))
      .andExpect(jsonPath(workPath + "['_contributorReference'][1]['type']")
        .value("http://bibfra.me/vocab/lite/Family"));
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
