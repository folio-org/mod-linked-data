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

class JurisdictionAuthorityIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 10,
            "http://bibfra.me/vocab/lite/name": ["Test Jurisdiction Name"],
            "http://bibfra.me/vocab/library/subordinateUnit": ["Sub Unit"],
            "http://bibfra.me/vocab/library/place": ["Test Place"],
            "http://bibfra.me/vocab/lite/date": ["1990-2020"],
            "http://bibfra.me/vocab/library/miscInfo": ["Jurisdiction info"],
            "http://bibfra.me/vocab/scholar/affiliation": ["Jurisdiction Org"]
          }
        }
      }""";
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(10))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Jurisdiction Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/subordinateUnit'][0]").value("Sub Unit"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/place'][0]").value("Test Place"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/date'][0]").value("1990-2020"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Jurisdiction info"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/scholar/affiliation'][0]").value("Jurisdiction Org"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Jurisdiction");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Jurisdiction Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/subordinateUnit")).isEqualTo("Sub Unit");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/place")).isEqualTo("Test Place");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/date")).isEqualTo("1990-2020");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Jurisdiction info");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/scholar/affiliation")).isEqualTo("Jurisdiction Org");
  }
}
