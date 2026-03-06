package org.folio.linked.data.service.vocabulary;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.VocabularyEntity;
import org.folio.linked.data.repo.VocabularyRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class VocabularyServiceImplTest {

  @InjectMocks
  private VocabularyServiceImpl vocabularyServiceImpl;
  @Mock
  private VocabularyRepository vocabularyRepository;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @Test
  void getVocabularyByName_shouldReturnVocabularyJsonWhenFound() {
    // given
    var vocabularyName = "test-vocabulary";
    var expectedJson = "{\"name\":\"test\"}";
    var vocabularyEntity = new VocabularyEntity()
      .setVocabularyName(vocabularyName)
      .setVocabularyJson(expectedJson);
    when(vocabularyRepository.findById(vocabularyName)).thenReturn(Optional.of(vocabularyEntity));

    // when
    var result = vocabularyServiceImpl.getVocabularyByName(vocabularyName);

    // then
    assertThat(result).isEqualTo(expectedJson);
  }

  @Test
  void getVocabularyByName_shouldThrowExceptionWhenVocabularyNotFound() {
    // given
    var vocabularyName = "missing-vocabulary";
    var exception = new RequestProcessingException(404, "NOT_FOUND", emptyMap(), "Vocabulary not found");
    when(vocabularyRepository.findById(vocabularyName)).thenReturn(Optional.empty());
    when(exceptionBuilder.notFoundLdResourceByIdException("Vocabulary", vocabularyName)).thenReturn(exception);

    // when + then
    assertThatThrownBy(() -> vocabularyServiceImpl.getVocabularyByName(vocabularyName))
      .isSameAs(exception);
  }
}
