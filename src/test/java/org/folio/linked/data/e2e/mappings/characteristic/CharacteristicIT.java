package org.folio.linked.data.e2e.mappings.characteristic;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

class CharacteristicIT extends PostResourceIT {
  private static final long DATABASE_ID = 2562238976785385516L;
  private static final long MAGAZINE_ID = 74601223165450105L;
  private static final long NEWSPAPER_ID = -1589748036134422574L;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Work":{
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/marc/characteristic":[
                  {
                     "http://bibfra.me/vocab/marc/term":[ "database" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/database" ]
                  }, {
                     "http://bibfra.me/vocab/marc/term":[ "magazine" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/mag" ]
                  }, {
                     "http://bibfra.me/vocab/marc/term":[ "newspaper" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/newspaper" ]
                  }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  protected void validateGraph(Resource work) {
    var characteristicResources = getOutgoingResources(work, "http://bibfra.me/vocab/marc/characteristic");
    var expectedCharacteristics = getExpectedCharacteristics();

    for (var resource : characteristicResources) {
      validateResourceType(resource, "http://bibfra.me/vocab/lite/Category");

      var actualDoc = objectMapper.convertValue(resource.getDoc(), new TypeReference<Map<String, Object>>() {});
      var expectedDoc = expectedCharacteristics.get(resource.getId());
      assertThat(actualDoc).isEqualTo(expectedDoc);
    }

    var categorySets = characteristicResources.stream()
      .map(r -> getFirstOutgoingResource(r, "http://bibfra.me/vocab/lite/isDefinedBy"))
      .collect(Collectors.toSet());

    assertThat(categorySets).hasSize(1);

    var categorySet = categorySets.iterator().next();
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/label"))
      .isEqualTo("Serial Publication Type");
    assertThat(getProperty(categorySet, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/mserialpubtype");
    validateResourceType(categorySet, "http://bibfra.me/vocab/lite/CategorySet");
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var expected = getExpectedCharacteristics().entrySet().stream()
      .map(entry -> {
        var map = new HashMap<String, Object>(entry.getValue());
        map.put("id", String.valueOf(entry.getKey()));
        return map;
      })
      .toList();

    var responseJson = apiResponse.andReturn().getResponse().getContentAsString();
    var characteristicsNode = objectMapper.readTree(responseJson)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("http://bibfra.me/vocab/marc/characteristic");

    var actual = objectMapper.convertValue(characteristicsNode, new TypeReference<List<Map<String, Object>>>() {});

    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  private Resource getFirstOutgoingResource(Resource instance, String url) {
    return getOutgoingResources(instance, url).getFirst();
  }

  private Map<Long, Map<String, List<String>>> getExpectedCharacteristics() {
    return Map.of(
      DATABASE_ID, Map.of(
        "http://bibfra.me/vocab/marc/term", List.of("database"),
        "http://bibfra.me/vocab/marc/code", List.of("d"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/database")
      ),
      MAGAZINE_ID, Map.of(
        "http://bibfra.me/vocab/marc/term", List.of("magazine"),
        "http://bibfra.me/vocab/marc/code", List.of("g"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/mag")
      ),
      NEWSPAPER_ID, Map.of(
        "http://bibfra.me/vocab/marc/term", List.of("newspaper"),
        "http://bibfra.me/vocab/marc/code", List.of("n"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/newspaper")
      )
    );
  }
}
