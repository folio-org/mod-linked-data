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

class OrganizationAuthorityIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 12,
            "http://bibfra.me/vocab/lite/name": ["Test Organization Name"],
            "http://bibfra.me/vocab/library/subordinateUnit": ["Org Sub Unit"],
            "http://bibfra.me/vocab/library/place": ["Org Place"],
            "http://bibfra.me/vocab/lite/date": ["1985-2023"],
            "http://bibfra.me/vocab/library/miscInfo": ["Organization info"],
            "http://bibfra.me/vocab/scholar/affiliation": ["Parent Org"]
          }
        }
      }""";
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(12))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Organization Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/subordinateUnit'][0]").value("Org Sub Unit"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/place'][0]").value("Org Place"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/date'][0]").value("1985-2023"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Organization info"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/scholar/affiliation'][0]").value("Parent Org"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Organization");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Organization Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/subordinateUnit")).isEqualTo("Org Sub Unit");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/place")).isEqualTo("Org Place");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/date")).isEqualTo("1985-2023");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Organization info");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/scholar/affiliation")).isEqualTo("Parent Org");
  }
}
