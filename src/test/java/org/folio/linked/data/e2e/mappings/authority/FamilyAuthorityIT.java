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

class FamilyAuthorityIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 8,
            "http://bibfra.me/vocab/lite/name": ["Test Family Name"],
            "http://bibfra.me/vocab/library/numeration": ["I"],
            "http://bibfra.me/vocab/library/titles": ["Dr."],
            "http://bibfra.me/vocab/lite/date": ["2000-2024"],
            "http://bibfra.me/vocab/library/miscInfo": ["Some family info"],
            "http://bibfra.me/vocab/library/attribution": ["Family attribution"],
            "http://bibfra.me/vocab/lite/nameAlternative": ["Alt Family Name"],
            "http://bibfra.me/vocab/scholar/affiliation": ["Family University"]
          }
        }
      }""";
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(8))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Family Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/numeration'][0]").value("I"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/titles'][0]").value("Dr."))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/date'][0]").value("2000-2024"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Some family info"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/attribution'][0]").value("Family attribution"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/nameAlternative'][0]").value("Alt Family Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/scholar/affiliation'][0]").value("Family University"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Family");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Family Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/numeration")).isEqualTo("I");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/titles")).isEqualTo("Dr.");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/date")).isEqualTo("2000-2024");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Some family info");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/attribution")).isEqualTo("Family attribution");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/nameAlternative")).isEqualTo("Alt Family Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/scholar/affiliation")).isEqualTo("Family University");
    assertThat(resource.getLabel()).isEqualTo("I, Test Family Name, Dr., Alt Family Name, 2000-2024");
  }
}
