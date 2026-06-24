package org.folio.linked.data.e2e.mappings.authority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.AUTHORITY_PATH;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.getOutgoingResources;
import static org.folio.linked.data.test.TestUtil.getProperty;
import static org.folio.linked.data.test.TestUtil.validateResourceType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

class FamilyAuthorityIT extends PostAuthorityIT {

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
            "http://bibfra.me/vocab/scholar/affiliation": ["Family University"],
            "http://bibfra.me/vocab/library/numberOfParts": ["Family Number of parts"],
            "http://library.link/vocab/map": [
              {
                "http://library.link/identifier/LCCN": {
                  "http://bibfra.me/vocab/lite/name": ["n2024001234"]
                }
              },
              {
                "http://library.link/identifier/UNKNOWN": {
                  "http://bibfra.me/vocab/lite/name": ["other-id-123"]
                }
              }
            ]
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
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/scholar/affiliation'][0]").value("Family University"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/numberOfParts'][0]").value("Family Number of parts"));

    var responsePayload = apiResponse.andReturn().getResponse().getContentAsString();
    var mapNode = TEST_JSON_MAPPER.readTree(responsePayload)
      .path("resource").path("_authority").path("http://library.link/vocab/map");
    assertThat(mapNode.get(0).path("http://library.link/identifier/LCCN")
      .path("http://bibfra.me/vocab/lite/name").get(0).asString()).isEqualTo("n2024001234");
    assertThat(mapNode.get(1).path("http://library.link/identifier/UNKNOWN")
      .path("http://bibfra.me/vocab/lite/name").get(0).asString()).isEqualTo("other-id-123");
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
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/numberOfParts")).isEqualTo("Family Number of parts");
    assertThat(resource.getLabel()).isEqualTo("I, Test Family Name, Dr., Alt Family Name, 2000-2024");

    var identifiers = getOutgoingResources(resource, "http://library.link/vocab/map");
    assertThat(identifiers).hasSize(2);

    var lccn = identifiers.stream()
      .filter(r -> r.getTypes().stream().anyMatch(t -> "http://library.link/identifier/LCCN".equals(t.getUri())))
      .findFirst().orElseThrow();
    validateResourceType(lccn, "http://bibfra.me/vocab/lite/Identifier", "http://library.link/identifier/LCCN");
    assertThat(getProperty(lccn, "http://bibfra.me/vocab/lite/name")).isEqualTo("n2024001234");
    assertThat(lccn.getLabel()).isEqualTo("n2024001234");

    var otherId = identifiers.stream()
      .filter(r -> r.getTypes().stream().anyMatch(t -> "http://library.link/identifier/UNKNOWN".equals(t.getUri())))
      .findFirst().orElseThrow();
    validateResourceType(otherId, "http://bibfra.me/vocab/lite/Identifier", "http://library.link/identifier/UNKNOWN");
    assertThat(getProperty(otherId, "http://bibfra.me/vocab/lite/name")).isEqualTo("other-id-123");
    assertThat(otherId.getLabel()).isEqualTo("other-id-123");
  }
}
