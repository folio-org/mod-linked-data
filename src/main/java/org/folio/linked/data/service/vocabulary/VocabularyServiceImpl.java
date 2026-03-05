package org.folio.linked.data.service.vocabulary;

import static org.folio.linked.data.util.Constants.Cache.VOCABULARIES;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.VocabularyEntity;
import org.folio.linked.data.repo.VocabularyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {
  private static final String VOCABULARIES_DIRECTORY = "vocabularies";

  private final VocabularyRepository vocabularyRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Transactional(readOnly = true)
  @Override
  @Cacheable(value = VOCABULARIES, key = "@folioExecutionContext.tenantId + '_' + #vocabularyName")
  public String getVocabularyByName(String vocabularyName) {
    return vocabularyRepository.findById(vocabularyName)
      .map(VocabularyEntity::getVocabularyJson)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Vocabulary", vocabularyName));
  }

  @Override
  @Transactional
  public void saveAllVocabularies() {
    var vocabulariesDirectory = getClass().getClassLoader().getResource(VOCABULARIES_DIRECTORY);
    if (vocabulariesDirectory == null) {
      log.warn("Vocabularies directory not found: {}", VOCABULARIES_DIRECTORY);
      return;
    }

    try (var files = Files.list(Paths.get(vocabulariesDirectory.toURI()))) {
      files
        .filter(Files::isRegularFile)
        .filter(path -> path.getFileName().toString().endsWith(".json"))
        .forEach(this::saveVocabulary);
    } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
      log.error("Failed to read vocabularies from directory: {}", VOCABULARIES_DIRECTORY, e);
    }
  }

  private void saveVocabulary(Path file) {
    try {
      var fileName = file.getFileName().toString();
      var vocabularyName = fileName.substring(0, fileName.length() - ".json".length());
      var vocabularyJson = Files.readString(file);

      var vocabulary = new VocabularyEntity()
        .setVocabularyName(vocabularyName)
        .setVocabularyJson(vocabularyJson);

      vocabularyRepository.save(vocabulary);
    } catch (IOException e) {
      log.error("Failed to process vocabulary file: {}", file, e);
    }
  }
}
