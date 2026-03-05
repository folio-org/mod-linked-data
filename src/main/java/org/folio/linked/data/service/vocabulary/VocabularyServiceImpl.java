package org.folio.linked.data.service.vocabulary;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.repo.VocabularyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

  private final VocabularyRepository vocabularyRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Transactional(readOnly = true)
  @Override
  public String getVocabularyByName(String vocabularyName) {
    return vocabularyRepository.findById(vocabularyName)
      .map(vocabulary -> vocabulary.getVocabularyJson())
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Vocabulary", vocabularyName));
  }
}
