package org.folio.linked.data.service.vocabulary;

import static org.folio.linked.data.util.Constants.Cache.VOCABULARIES;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.VocabularyEntity;
import org.folio.linked.data.repo.VocabularyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {
  private static final String VOCABULARIES_PATTERN = "classpath*:vocabularies/*.json";

  private final VocabularyRepository vocabularyRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourcePatternResolver resourcePatternResolver;

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
    try {
      var resources = resourcePatternResolver.getResources(VOCABULARIES_PATTERN);
      if (resources.length == 0) {
        log.error("Vocabulary files not found: {}", VOCABULARIES_PATTERN);
        return;
      }

      for (var resource : resources) {
        saveVocabulary(resource);
      }
    } catch (IOException e) {
      log.error("Failed to read vocabularies with pattern: {}", VOCABULARIES_PATTERN, e);
    }
  }

  private void saveVocabulary(Resource resource) {
    try (var inputStream = resource.getInputStream()) {
      var fileName = resource.getFilename();
      if (fileName == null || !fileName.endsWith(".json")) {
        log.warn("Skipping vocabulary resource with invalid filename: {}", resource);
        return;
      }

      var vocabularyName = fileName.substring(0, fileName.length() - ".json".length());
      var vocabularyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

      var vocabulary = new VocabularyEntity()
        .setVocabularyName(vocabularyName)
        .setVocabularyJson(vocabularyJson);

      vocabularyRepository.save(vocabulary);
    } catch (IOException e) {
      log.error("Failed to process vocabulary resource: {}", resource, e);
    }
  }
}
