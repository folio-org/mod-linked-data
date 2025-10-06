package org.folio.linked.data.e2e.mappings.instance.adminmetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Set;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public class AdminMetadataWithControlNumberIT extends PostResourceIT {
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
               "http://bibfra.me/vocab/library/adminMetadata":[
                  {
                     "http://bibfra.me/vocab/library/controlNumber":[ "0987654321" ],
                     "http://bibfra.me/vocab/lite/createdDate": ["2025-08-05"],
                     "http://bibfra.me/vocab/library/catalogingAgency": ["Agency 1"],
                     "http://bibfra.me/vocab/library/transcribingAgency": ["Agency 2"],
                     "http://bibfra.me/vocab/library/modifyingAgency": ["Agency 3", "Agency 4"],
                     "http://bibfra.me/vocab/lite/catalogingLanguage": [
                        {
                           "http://bibfra.me/vocab/library/term": [ "English" ],
                           "http://bibfra.me/vocab/lite/link": [ "http://id.loc.gov/vocabulary/languages/eng" ]
                        }
                     ]
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
    var path = "$.resource['http://bibfra.me/vocab/lite/Instance']['http://bibfra.me/vocab/library/adminMetadata'][0]";
    var catalogingLanguagePath = path + "['http://bibfra.me/vocab/lite/catalogingLanguage'][0]";
    apiResponse
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/library/controlNumber'][0]").value("0987654321"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/lite/createdDate'][0]").value("2025-08-05"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/library/catalogingAgency'][0]").value("Agency 1"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/library/transcribingAgency'][0]").value("Agency 2"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/library/modifyingAgency'][0]").value("Agency 3"))
      .andExpect(jsonPath(path + "['http://bibfra.me/vocab/library/modifyingAgency'][1]").value("Agency 4"))
      .andExpect(jsonPath(catalogingLanguagePath + "['http://bibfra.me/vocab/library/code']").value("eng"))
      .andExpect(jsonPath(catalogingLanguagePath + "['http://bibfra.me/vocab/library/term']").value("English"))
      .andExpect(jsonPath(catalogingLanguagePath + "['http://bibfra.me/vocab/lite/link']")
        .value("http://id.loc.gov/vocabulary/languages/eng"));
  }

  @Override
  protected void validateGraph(Resource instance) {
    var expectedAminMetadataId = -3269956642629451736L;
    var adminMetadata = getFirstOutgoingResource(instance, "http://bibfra.me/vocab/library/adminMetadata");
    validateResourceType(adminMetadata, "http://bibfra.me/vocab/lite/Annotation");
    assertThat(adminMetadata.getId()).isEqualTo(expectedAminMetadataId);
    assertThat(adminMetadata.getLabel()).isEqualTo("0987654321");
    assertThat(getProperty(adminMetadata, "http://bibfra.me/vocab/library/controlNumber")).isEqualTo("0987654321");
    assertThat(getProperty(adminMetadata, "http://bibfra.me/vocab/lite/createdDate")).isEqualTo("2025-08-05");
    assertThat(getProperty(adminMetadata, "http://bibfra.me/vocab/library/catalogingAgency")).isEqualTo("Agency 1");
    assertThat(getProperty(adminMetadata, "http://bibfra.me/vocab/library/transcribingAgency")).isEqualTo("Agency 2");
    assertThat(getProperties(adminMetadata, "http://bibfra.me/vocab/library/modifyingAgency"))
      .isEqualTo(Set.of("Agency 3", "Agency 4"));

    var expectedLanguageId = -878606130574011566L;
    var catalogingLanguage = getFirstOutgoingResource(adminMetadata, "http://bibfra.me/vocab/lite/catalogingLanguage");
    validateResourceType(catalogingLanguage, "http://bibfra.me/vocab/lite/LanguageCategory");
    assertThat(catalogingLanguage.getId()).isEqualTo(expectedLanguageId);
    assertThat(catalogingLanguage.getLabel()).isEqualTo("eng");
    assertThat(getProperty(catalogingLanguage, "http://bibfra.me/vocab/library/code")).isEqualTo("eng");
    assertThat(getProperty(catalogingLanguage, "http://bibfra.me/vocab/library/term")).isEqualTo("English");
    assertThat(getProperty(catalogingLanguage, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/languages/eng");
  }
}
