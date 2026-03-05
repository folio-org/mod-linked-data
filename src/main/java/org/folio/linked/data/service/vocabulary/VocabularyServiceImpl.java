package org.folio.linked.data.service.vocabulary;

import static org.folio.linked.data.util.Constants.Cache.VOCABULARIES;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.VocabularyEntity;
import org.folio.linked.data.repo.VocabularyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

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
}
