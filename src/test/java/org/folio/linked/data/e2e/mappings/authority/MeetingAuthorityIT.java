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

class MeetingAuthorityIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 11,
            "http://bibfra.me/vocab/lite/name": ["Test Meeting Name"],
            "http://bibfra.me/vocab/library/place": ["Meeting Place"],
            "http://bibfra.me/vocab/lite/date": ["2010"],
            "http://bibfra.me/vocab/library/subordinateUnit": ["Meeting Sub Unit"],
            "http://bibfra.me/vocab/library/miscInfo": ["Meeting info"],
            "http://bibfra.me/vocab/scholar/affiliation": ["Meeting Org"]
          }
        }
      }""";
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    apiResponse
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(11))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Meeting Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/place'][0]").value("Meeting Place"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/date'][0]").value("2010"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/subordinateUnit'][0]").value("Meeting Sub Unit"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Meeting info"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/scholar/affiliation'][0]").value("Meeting Org"));
  }

  @Override
  protected void validateGraph(Resource resource) {
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Meeting");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Meeting Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/place")).isEqualTo("Meeting Place");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/date")).isEqualTo("2010");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/subordinateUnit")).isEqualTo("Meeting Sub Unit");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Meeting info");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/scholar/affiliation")).isEqualTo("Meeting Org");
  }
}
