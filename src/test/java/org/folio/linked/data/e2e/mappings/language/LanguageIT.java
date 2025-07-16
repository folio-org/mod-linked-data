package org.folio.linked.data.e2e.mappings.language;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

public class LanguageIT extends PostResourceIT {

  @Autowired
  private ObjectMapper mapper;

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
           "_languages":[
            {
             "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/languages/eng" ],
             "http://bibfra.me/vocab/marc/term":[ "English" ],
             "_types":[
              "http://bibfra.me/vocab/lite/language",
              "http://bibfra.me/vocab/lite/accompanyingMaterialLanguage"
             ]
            },
            {
             "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/languages/jpn" ],
             "http://bibfra.me/vocab/marc/term":[ "Japanese" ],
             "_types":[
              "http://bibfra.me/vocab/lite/accompanyingMaterialLanguage",
              "http://bibfra.me/vocab/lite/originalLanguage"
             ]
            },
            {
             "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/languages/spa" ],
             "http://bibfra.me/vocab/marc/term":[ "Spanish" ],
             "_types":[ "http://bibfra.me/vocab/lite/tableOfContentsLanguage" ]
            }
           ]
          }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var expectedLanguages = getExpectedLanguages();
    var actualLanguages = getActualLanguages(apiResponse);

    expectedLanguages.forEach((id, expectedLanguage) -> {
      var actualLanguage = actualLanguages.stream()
        .filter(lang -> lang.getId().equals(String.valueOf(id)))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Language with id " + id + " not found"));

      assert actualLanguage.getLink().contains(expectedLanguage.link);
      assert actualLanguage.getCode().contains(expectedLanguage.code);
      assert actualLanguage.getTerm().contains(expectedLanguage.term);
      assert actualLanguage.getTypes().containsAll(expectedLanguage.types);
    });
  }

  @Override
  protected void validateGraph(Resource resource) {
    var primaryLanguage = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/lite/language");
    validateLanguage(primaryLanguage);

    var originalLanguage = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/lite/originalLanguage");
    validateLanguage(originalLanguage);

    var tocLanguage = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/lite/tableOfContentsLanguage");
    validateLanguage(tocLanguage);

    var accMatLanguages = getOutgoingResources(resource, "http://bibfra.me/vocab/lite/accompanyingMaterialLanguage");
    assertThat(accMatLanguages).contains(primaryLanguage, originalLanguage);
  }

  private static Map<Long, LanguageCategory> getExpectedLanguages() {
    return Map.of(
      6136110693438399146L, new LanguageCategory(
        "http://id.loc.gov/vocabulary/languages/eng",
        "eng",
        "English",
        Set.of("http://bibfra.me/vocab/lite/language", "http://bibfra.me/vocab/lite/accompanyingMaterialLanguage")
      ),

      -8414775996109135490L, new LanguageCategory(
        "http://id.loc.gov/vocabulary/languages/jpn",
        "jpn",
        "Japanese",
        Set.of("http://bibfra.me/vocab/lite/accompanyingMaterialLanguage",
          "http://bibfra.me/vocab/lite/originalLanguage")),

      -2319052759516826290L, new LanguageCategory(
        "http://id.loc.gov/vocabulary/languages/spa",
        "spa",
        "Spanish",
        Set.of("http://bibfra.me/vocab/lite/tableOfContentsLanguage"))
    );
  }

  @SneakyThrows
  private List<Language> getActualLanguages(ResultActions apiResponse) {
    var responseStr = apiResponse.andReturn().getResponse().getContentAsString();
    var languagesNode = mapper.readTree(responseStr)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("_languages");

    return mapper.convertValue(
      languagesNode,
      mapper.getTypeFactory().constructCollectionType(List.class, Language.class)
    );
  }

  private void validateLanguage(Resource language) {
    validateResourceType(language, "http://bibfra.me/vocab/lite/LanguageCategory");

    var expected = getExpectedLanguages().get(language.getId());
    assertThat(getProperty(language, "http://bibfra.me/vocab/lite/link")).isEqualTo(expected.link);
    assertThat(getProperty(language, "http://bibfra.me/vocab/marc/term")).isEqualTo(expected.term);
    assertThat(getProperty(language, "http://bibfra.me/vocab/marc/code")).isEqualTo(expected.code);
    assertThat(language.getLabel()).isEqualTo(expected.code);
  }

  private Resource getFirstOutgoingResource(Resource work, String url) {
    return getOutgoingResources(work, url).getFirst();
  }

  private record LanguageCategory(
    String link,
    String code,
    String term,
    Set<String> types) {
  }
}
