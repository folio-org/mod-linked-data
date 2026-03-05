package org.folio.linked.data.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.service.vocabulary.VocabularyService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class VocabularyControllerTest {

  @InjectMocks
  private VocabularyController vocabularyController;
  @Mock
  private VocabularyService vocabularyService;

  @Test
  void getVocabularyByName_shouldReturnOkResponse() throws Exception {
    // given
    var vocabularyName = "test-vocabulary";
    var expectedVocabularyJson = "{\"test\":\"value\"}";
    when(vocabularyService.getVocabularyByName(vocabularyName)).thenReturn(expectedVocabularyJson);
    var mockMvc = MockMvcBuilders.standaloneSetup(vocabularyController).build();

    // when
    mockMvc.perform(get("/linked-data/vocabularies/{vocabularyName}", vocabularyName))
      .andExpect(status().isOk())
      .andExpect(content().json(expectedVocabularyJson));

    // then
    verify(vocabularyService).getVocabularyByName(vocabularyName);
  }
}
