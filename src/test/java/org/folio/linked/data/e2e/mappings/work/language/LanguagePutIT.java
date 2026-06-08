package org.folio.linked.data.e2e.mappings.work.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getFirstOutgoingResource;
import static org.folio.linked.data.test.TestUtil.getOutgoingResources;
import static org.folio.linked.data.test.TestUtil.getProperty;
import static org.folio.linked.data.test.TestUtil.getResourceId;
import static org.folio.linked.data.test.TestUtil.validateResourceType;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import lombok.SneakyThrows;
import org.springframework.test.web.servlet.ResultActions;
import org.folio.linked.data.domain.dto.LanguageWithType;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
class LanguagePutIT extends ITBase {

  private static final String RESOURCE_URL = "/linked-data/resource";
  private static final int RESOURCE_FETCH_DEPTH = 4;

  @Test
  void shouldUpdateLanguages() throws Exception {
    // given
    var existingWork = resourceTestService.saveGraph(getSampleWork());

    var putRequest = put(RESOURCE_URL + "/" + existingWork.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(putPayload());

    // when
    var putResponse = mockMvc.perform(putRequest);

    // then
    putResponse.andExpect(status().isOk());
    validateUpdatedApiResponse(putResponse);
    var updatedResourceId = getResourceId(putResponse);
    var updatedResource = resourceTestService.getResourceById(updatedResourceId, RESOURCE_FETCH_DEPTH);
    validateUpdatedGraph(updatedResource);
  }

  private String putPayload() {
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
           "_languages":[
            {
             "_codes":[{
              "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/languages/fre" ],
              "http://bibfra.me/vocab/library/term":[ "French" ]
             }],
             "_types":[ "http://bibfra.me/vocab/lite/language" ]
            }
           ]
          }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @SneakyThrows
  private void validateUpdatedApiResponse(ResultActions apiResponse) {
    var actualLanguages = getActualLanguages(apiResponse);

    assertThat(actualLanguages).hasSize(1);
    var language = actualLanguages.get(0);
    assertThat(language.getCodes()).hasSize(1);
    var code = language.getCodes().get(0);
    assertThat(code.getLink()).containsExactly("http://id.loc.gov/vocabulary/languages/fre");
    assertThat(code.getTerm()).containsExactly("French");
    assertThat(code.getCode()).containsExactly("fre");
    assertThat(language.getTypes()).containsExactly("http://bibfra.me/vocab/lite/language");
  }

  private void validateUpdatedGraph(Resource resource) {
    var frenchLanguage = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/lite/language");
    validateResourceType(frenchLanguage, "http://bibfra.me/vocab/lite/LanguageCategory");
    assertThat(getProperty(frenchLanguage, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/languages/fre");
    assertThat(getProperty(frenchLanguage, "http://bibfra.me/vocab/library/term")).isEqualTo("French");
    assertThat(getProperty(frenchLanguage, "http://bibfra.me/vocab/library/code")).isEqualTo("fre");
    assertThat(frenchLanguage.getLabel()).isEqualTo("fre");
    assertThat(getOutgoingResources(resource, "http://bibfra.me/vocab/lite/originalLanguage")).isEmpty();
    assertThat(getOutgoingResources(resource, "http://bibfra.me/vocab/lite/tableOfContentsLanguage")).isEmpty();
    assertThat(getOutgoingResources(resource, "http://bibfra.me/vocab/lite/accompanyingMaterialLanguage")).isEmpty();
  }

  @SneakyThrows
  private List<LanguageWithType> getActualLanguages(ResultActions apiResponse) {
    var responseStr = apiResponse.andReturn().getResponse().getContentAsString();
    var languagesNode = TEST_JSON_MAPPER.readTree(responseStr)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("_languages");

    return TEST_JSON_MAPPER.convertValue(languagesNode,
      TEST_JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, LanguageWithType.class)
    );
  }
}
