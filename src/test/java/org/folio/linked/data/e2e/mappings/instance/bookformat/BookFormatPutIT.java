package org.folio.linked.data.e2e.mappings.instance.bookformat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getWork;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getFirstOutgoingResource;
import static org.folio.linked.data.test.TestUtil.getProperty;
import static org.folio.linked.data.test.TestUtil.getResourceId;
import static org.folio.linked.data.test.TestUtil.validateResourceType;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
class BookFormatPutIT extends ITBase {

  private static final String RESOURCE_URL = "/linked-data/resource";
  private static final int RESOURCE_FETCH_DEPTH = 4;

  @Test
  @SneakyThrows
  void shouldUpdateBookFormats() {
    // given
    var work = resourceTestService.saveGraph(getWork("Test Work", hashService));
    var existingInstance = resourceTestService.saveGraph(buildInstance(work));

    var putRequest = put(RESOURCE_URL + "/" + existingInstance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(putPayload(work.getId()));

    // when
    var putResponse = mockMvc.perform(putRequest);

    // then
    putResponse.andExpect(status().isOk());
    validateUpdatedApiResponse(putResponse);
    var updatedResourceId = getResourceId(putResponse);
    var updatedResource = resourceTestService.getResourceById(updatedResourceId, RESOURCE_FETCH_DEPTH);
    validateUpdatedGraph(updatedResource);
  }

  private Resource buildInstance(Resource work) {
    var titleDoc = TEST_JSON_MAPPER.createObjectNode();
    titleDoc.putArray("http://bibfra.me/vocab/library/mainTitle").add("TEST: BookFormatPutIT");
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(titleDoc)
      .setLabel("TEST: BookFormatPutIT");

    var instance = new Resource()
      .addTypes(ResourceTypeDictionary.INSTANCE)
      .setDoc(TEST_JSON_MAPPER.createObjectNode())
      .setLabel("TEST: BookFormatPutIT");
    instance.addOutgoingEdge(new ResourceEdge(instance, title, PredicateDictionary.TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, PredicateDictionary.INSTANTIATES));

    title.setIdAndRefreshEdges(hashService.hash(title));
    instance.setIdAndRefreshEdges(hashService.hash(instance));
    instance.setFolioMetadata(new FolioMetadata(instance)
      .setInventoryId(UUID.randomUUID().toString())
      .setSrsId(UUID.randomUUID().toString()));
    return instance;
  }

  private String putPayload(Long workId) {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "profileId": 3,
               "http://bibfra.me/vocab/library/title":[
                  {
                     "http://bibfra.me/vocab/library/Title":{
                        "http://bibfra.me/vocab/library/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/library/bookFormat":[
                  {
                     "http://bibfra.me/vocab/library/term":[ "8vo" ],
                     "http://bibfra.me/vocab/lite/link": ["http://id.loc.gov/vocabulary/bookformat/8vo"]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "updated-non-standard" ]
                  }
               ],
               "_workReference": [ { "id": "%s" } ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName(), workId);
  }

  @SneakyThrows
  private void validateUpdatedApiResponse(ResultActions apiResponse) {
    var bookFormatPath = "$.resource['http://bibfra.me/vocab/lite/Instance']['http://bibfra.me/vocab/library/bookFormat']";
    apiResponse
      .andExpect(jsonPath(bookFormatPath + "[0]['http://bibfra.me/vocab/library/term'][0]")
        .value("updated-non-standard"))
      .andExpect(jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/library/term'][0]")
        .value("8vo"))
      .andExpect(jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/library/code'][0]")
        .value("8vo"))
      .andExpect(jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/lite/link'][0]")
        .value("http://id.loc.gov/vocabulary/bookformat/8vo"));
  }

  private void validateUpdatedGraph(Resource instance) {
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/bookFormat"))
      .isEqualTo("updated-non-standard");

    var bookFormat = getFirstOutgoingResource(instance, "http://bibfra.me/vocab/library/bookFormat");
    validateResourceType(bookFormat, "http://bibfra.me/vocab/lite/Category");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/library/term")).isEqualTo("8vo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/library/code")).isEqualTo("8vo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat/8vo");
    assertThat(bookFormat.getLabel()).isEqualTo("8vo");

    var categorySet = getFirstOutgoingResource(bookFormat, "http://bibfra.me/vocab/lite/isDefinedBy");
    validateResourceType(categorySet, "http://bibfra.me/vocab/lite/CategorySet");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label")).isEqualTo("Book Format");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat");
    assertThat(categorySet.getLabel()).isEqualTo("Book Format");
  }
}
