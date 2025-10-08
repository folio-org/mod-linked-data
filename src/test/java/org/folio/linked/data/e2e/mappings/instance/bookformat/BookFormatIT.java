package org.folio.linked.data.e2e.mappings.instance.bookformat;

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
                     "http://bibfra.me/vocab/library/term":[ "128mo" ],
                     "http://bibfra.me/vocab/lite/link": ["http://id.loc.gov/vocabulary/bookformat/128mo"]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "non-standard-format" ]
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
    var bookFormatPath = "$.resource['http://bibfra.me/vocab/lite/Instance']['http://bibfra.me/vocab/library/bookFormat']";
    apiResponse
      .andExpect(
        jsonPath(bookFormatPath + "[0]['http://bibfra.me/vocab/library/term'][0]")
          .value("non-standard-format"))
      .andExpect(jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/library/term'][0]")
        .value("128mo"))
      .andExpect(
        jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/library/code'][0]")
          .value("128mo"))
      .andExpect(
        jsonPath(bookFormatPath + "[1]['http://bibfra.me/vocab/lite/link'][0]")
          .value("http://id.loc.gov/vocabulary/bookformat/128mo"));
  }

  @Override
  protected void validateGraph(Resource instance) {
    final var expectedBookFormatId = -3739221851083272823L;
    final var expectedCategorySetId = -5037749211942465056L;
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/bookFormat"))
      .isEqualTo("non-standard-format");

    var bookFormat = getFirstOutgoingResource(instance, "http://bibfra.me/vocab/library/bookFormat");
    assertThat(bookFormat.getId()).isEqualTo(expectedBookFormatId);
    validateResourceType(bookFormat, "http://bibfra.me/vocab/lite/Category");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/library/term")).isEqualTo("128mo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/library/code")).isEqualTo("128mo");
    assertThat(getProperty(bookFormat, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat/128mo");
    assertThat(bookFormat.getLabel()).isEqualTo("128mo");

    var categorySet = getFirstOutgoingResource(bookFormat, "http://bibfra.me/vocab/lite/isDefinedBy");
    assertThat(categorySet.getId()).isEqualTo(expectedCategorySetId);
    validateResourceType(categorySet, "http://bibfra.me/vocab/lite/CategorySet");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label")).isEqualTo("Book Format");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/bookformat");
    assertThat(categorySet.getLabel()).isEqualTo("Book Format");
  }
}
