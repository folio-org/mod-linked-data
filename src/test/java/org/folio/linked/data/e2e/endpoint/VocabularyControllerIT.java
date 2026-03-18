package org.folio.linked.data.e2e.endpoint;

import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class VocabularyControllerIT {

  private static final String VOCABULARIES_ENDPOINT = "/linked-data/vocabularies";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;

  @Test
  @SneakyThrows
  void getVocabularyByName_shouldReturnSameJsonAsVocabularyFile() {
    var vocabulariesDirectory = getClass().getClassLoader().getResource("vocabularies");
    assertNotNull(vocabulariesDirectory, "Vocabularies directory is missing from classpath");

    try (var files = Files.list(Paths.get(vocabulariesDirectory.toURI()))) {
      var vocabularyFiles = files
        .filter(Files::isRegularFile)
        .toList();
      assertFalse(vocabularyFiles.isEmpty(), "No vocabulary JSON files found in vocabularies directory");

      for (var file : vocabularyFiles) {
        var fileName = file.getFileName().toString();
        var vocabularyName = fileName.substring(0, fileName.length() - ".json".length());
        var expectedJson = Files.readString(file);

        var requestBuilder = get(VOCABULARIES_ENDPOINT + "/{vocabularyName}", vocabularyName)
          .headers(defaultHeaders(env));

        var actualJson = mockMvc.perform(requestBuilder)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andReturn()
          .getResponse()
          .getContentAsString();

        JSONAssert.assertEquals(
          "Vocabulary JSON mismatch for file '%s'".formatted(fileName),
          expectedJson,
          actualJson,
          JSONCompareMode.STRICT);
      }
    }
  }
}
