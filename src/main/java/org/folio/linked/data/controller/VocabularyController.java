package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.VocabularyApi;
import org.folio.linked.data.service.vocabulary.VocabularyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VocabularyController implements VocabularyApi {

  private final VocabularyService vocabularyService;

  @Override
  public ResponseEntity<String> getVocabularyByName(String vocabularyName) {
    return ResponseEntity.ok(vocabularyService.getVocabularyByName(vocabularyName));
  }
}
