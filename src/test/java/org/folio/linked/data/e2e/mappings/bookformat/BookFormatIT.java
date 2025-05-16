package org.folio.linked.data.e2e.mappings.bookformat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
class BookFormatIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/marc/bookFormat":[
                  {
                     "http://bibfra.me/vocab/marc/term":[ "128mo" ],
                     "http://bibfra.me/vocab/lite/link": ["http://id.loc.gov/vocabulary/bookformat/128mo"]
                  }, {
                     "http://bibfra.me/vocab/marc/term":[ "non-standard-format" ]
                  }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse)  {
    var bookFormatPath = "$.resource['http://bibfra.me/vocab/lite/Instance']['http://bibfra.me/vocab/marc/bookFormat']";
    apiResponse
      .andExpect(
        jsonPath(bookFormatPath + "[0]['http://bibfra.me/vocab/marc/term'][0]")
          .value("non-standard-format"))
      .andExpect(jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/marc/term'][0]")
        .value("128mo"))
      .andExpect(
        jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/marc/code'][0]")
          .value("128mo"))
      .andExpect(
        jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/lite/link'][0]")
          .value("http://id.loc.gov/vocabulary/bookformat/128mo"));
  }

  @Override
  protected void validateGraph(Resource instance) {
    var expectedBookFormatId = 1710735011707999802L;
    var expectedCategorySetId = -5037749211942465056L;
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/bookFormat"))
      .isEqualTo("non-standard-format");

    var bookFormat = getFirstOutgoingResource(instance, "http://bibfra.me/vocab/marc/bookFormat");
    assertThat(bookFormat.getId()).isEqualTo(expectedBookFormatId);
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/marc/term")).isEqualTo("128mo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/marc/code")).isEqualTo("128mo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat/128mo");
    assertThat(bookFormat.getLabel()).isEqualTo("128mo");

    var categorySet = getFirstOutgoingResource(bookFormat, "http://bibfra.me/vocab/lite/isDefinedBy");
    assertThat(categorySet.getId()).isEqualTo(expectedCategorySetId);
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label")).isEqualTo("Book Format");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat");
    assertThat(categorySet.getLabel()).isEqualTo("Book Format");
  }

  private Resource getFirstOutgoingResource(Resource instance, String url) {
    return getOutgoingResources(instance, url).getFirst();
  }
}
