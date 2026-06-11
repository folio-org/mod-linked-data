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

class PlaceAuthorityIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 14,
            "http://bibfra.me/vocab/lite/name": ["Test Place Name"],
            "http://bibfra.me/vocab/library/miscInfo": ["Place info"]
          }
        }
      }""";
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(14))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Place Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Place info"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Place");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Place Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Place info");
  }
}
