package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.folio.linked.data.service.vocabulary.VocabularyService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class VocabularyControllerTest {

  @InjectMocks
  private VocabularyController vocabularyController;
  @Mock
  private VocabularyService vocabularyService;

  @Test
  void getVocabularyByName_shouldReturnOkResponse() {
    // given
    var vocabularyName = "test-vocabulary";
    var expectedVocabularyJson = "{\"test\":\"value\"}";
    when(vocabularyService.getVocabularyByName(vocabularyName)).thenReturn(expectedVocabularyJson);

    // when
    var response = vocabularyController.getVocabularyByName(vocabularyName);

    // then
    assertThat(response)
      .isNotNull()
      .hasFieldOrPropertyWithValue("statusCode.value", 200)
      .hasFieldOrPropertyWithValue("body", expectedVocabularyJson);
  }
}
