package org.folio.linked.data.e2e.mappings.work.characteristic;

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
  private static final long BLOG_ID = 4634629703061696745L;
  private static final long DATABASE_ID = -1376966138356251815L;
  private static final long DIRECTORY_ID = -5644652921552477832L;
  private static final long JOURNAL_ID = -9109000455196759282L;
  private static final long LOOSE_LEAF_ID = -8131776622473217017L;
  private static final long MAGAZINE_ID = 5630319398424717907L;
  private static final long MONOGRAPHIC_SERIES_ID = 5959324705494798474L;
  private static final long NEWSLETTER_ID = 2451413445742678900L;
  private static final long NEWSPAPER_ID = -2662813991778205265L;
  private static final long PERIODICAL_ID = 2688342640394376770L;
  private static final long REPOSITORY_ID = -4707533701123454052L;
  private static final long WEBSITE_ID = 2549344889486893314L;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Work":{
               "profileId": 2,
               "http://bibfra.me/vocab/library/title":[
                  {
                     "http://bibfra.me/vocab/library/Title":{
                        "http://bibfra.me/vocab/library/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/library/characteristic":[
                  {
                     "http://bibfra.me/vocab/library/term":[ "database" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/database" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "magazine" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/mag" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "blog" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/blog" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "journal" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/journal" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "loose leaf" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/looseleaf" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "monographic series" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/monoseries" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "newspaper" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/newspaper" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "periodical" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/periodical" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "repository" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/repo" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "newsletter" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/newsletter" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "directory" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/direct" ]
                  }, {
                     "http://bibfra.me/vocab/library/term":[ "web site" ],
                     "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mserialpubtype/web" ]
                  }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  protected void validateGraph(Resource work) {
    var characteristicResources = getOutgoingResources(work, "http://bibfra.me/vocab/library/characteristic");
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
      .path("http://bibfra.me/vocab/library/characteristic");

    var actual = objectMapper.convertValue(characteristicsNode, new TypeReference<List<Map<String, Object>>>() {});

    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  private Map<Long, Map<String, List<String>>> getExpectedCharacteristics() {
    return Map.ofEntries(
      Map.entry(DATABASE_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("database"),
        "http://bibfra.me/vocab/library/code", List.of("d"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/database")
      )),
      Map.entry(MAGAZINE_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("magazine"),
        "http://bibfra.me/vocab/library/code", List.of("g"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/mag")
      )),
      Map.entry(NEWSPAPER_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("newspaper"),
        "http://bibfra.me/vocab/library/code", List.of("n"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/newspaper")
      )),
      Map.entry(JOURNAL_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("journal"),
        "http://bibfra.me/vocab/library/code", List.of("j"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/journal")
      )),
      Map.entry(BLOG_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("blog"),
        "http://bibfra.me/vocab/library/code", List.of("h"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/blog")
      )),
      Map.entry(PERIODICAL_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("periodical"),
        "http://bibfra.me/vocab/library/code", List.of("p"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/periodical")
      )),
      Map.entry(LOOSE_LEAF_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("loose leaf"),
        "http://bibfra.me/vocab/library/code", List.of("l"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/looseleaf")
      )),
      Map.entry(MONOGRAPHIC_SERIES_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("monographic series"),
        "http://bibfra.me/vocab/library/code", List.of("m"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/monoseries")
      )),
      Map.entry(NEWSLETTER_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("newsletter"),
        "http://bibfra.me/vocab/library/code", List.of("s"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/newsletter")
      )),
      Map.entry(DIRECTORY_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("directory"),
        "http://bibfra.me/vocab/library/code", List.of("t"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/direct")
      )),
      Map.entry(REPOSITORY_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("repository"),
        "http://bibfra.me/vocab/library/code", List.of("r"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/repo")
      )),
      Map.entry(WEBSITE_ID, Map.of(
        "http://bibfra.me/vocab/library/term", List.of("web site"),
        "http://bibfra.me/vocab/library/code", List.of("w"),
        "http://bibfra.me/vocab/lite/link", List.of("http://id.loc.gov/vocabulary/mserialpubtype/web")
      ))
    );
  }
}
