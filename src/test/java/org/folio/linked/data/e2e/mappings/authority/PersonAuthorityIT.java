package org.folio.linked.data.e2e.mappings.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.AUTHORITY_PATH;
import static org.folio.linked.data.test.TestUtil.getProperty;
import static org.folio.linked.data.test.TestUtil.validateResourceType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class PersonAuthorityIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 13,
            "http://bibfra.me/vocab/lite/name": ["Test Person Name"],
            "http://bibfra.me/vocab/library/numeration": ["II"],
            "http://bibfra.me/vocab/library/titles": ["Prof."],
            "http://bibfra.me/vocab/lite/date": ["1970-2024"],
            "http://bibfra.me/vocab/library/miscInfo": ["Person info"],
            "http://bibfra.me/vocab/library/attribution": ["Person attribution"],
            "http://bibfra.me/vocab/lite/nameAlternative": ["Alt Person Name"],
            "http://bibfra.me/vocab/scholar/affiliation": ["Person University"]
          }
        }
      }""";
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(13))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Person Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/numeration'][0]").value("II"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/titles'][0]").value("Prof."))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/date'][0]").value("1970-2024"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Person info"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/attribution'][0]").value("Person attribution"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/nameAlternative'][0]").value("Alt Person Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/scholar/affiliation'][0]").value("Person University"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Person");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Person Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/numeration")).isEqualTo("II");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/titles")).isEqualTo("Prof.");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/date")).isEqualTo("1970-2024");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Person info");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/attribution")).isEqualTo("Person attribution");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/nameAlternative")).isEqualTo("Alt Person Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/scholar/affiliation")).isEqualTo("Person University");
  }
}
