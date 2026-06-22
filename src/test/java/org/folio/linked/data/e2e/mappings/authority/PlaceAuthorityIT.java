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

class PlaceAuthorityIT extends PostAuthorityIT {

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "_authority": {
            "profileId": 14,
            "http://bibfra.me/vocab/lite/name": ["Test Place Name"],
            "http://bibfra.me/vocab/library/miscInfo": ["Place info"],
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
      .andExpect(jsonPath(AUTHORITY_PATH + "['profileId']").value(14))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/lite/name'][0]").value("Test Place Name"))
      .andExpect(jsonPath(AUTHORITY_PATH + "['http://bibfra.me/vocab/library/miscInfo'][0]").value("Place info"));

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
    validateResourceType(resource, "http://bibfra.me/vocab/lite/Place");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/lite/name")).isEqualTo("Test Place Name");
    assertThat(getProperty(resource, "http://bibfra.me/vocab/library/miscInfo")).isEqualTo("Place info");
    assertThat(resource.getLabel()).isEqualTo("Test Place Name");

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
