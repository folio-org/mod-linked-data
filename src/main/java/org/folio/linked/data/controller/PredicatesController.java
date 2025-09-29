package org.folio.linked.data.controller;

import java.util.Arrays;
import java.util.List;
import org.folio.ld.dictionary.PredicateDictionary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/linked-data/predicates")
public class PredicatesController {
  @GetMapping
  public ResponseEntity<List<PredicateDictionary>> predicates() {
    return ResponseEntity.ok(Arrays.asList(PredicateDictionary.values()));
  }
}
