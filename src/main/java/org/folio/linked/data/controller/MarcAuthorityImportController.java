package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.service.resource.MarcAuthorityImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/graph/import")
@RequiredArgsConstructor
public class MarcAuthorityImportController {
  private static final Double SIMILARITY_THRESHOLD = 0.80;

  private final MarcAuthorityImportService marcAuthorityImportService;

  @PostMapping("/from-marc-authority")
  @SneakyThrows
  public ResponseEntity<Object> importFromMarc(@RequestBody String marc, @RequestParam(value = "force", defaultValue = "false") boolean force) {
    var result = marcAuthorityImportService.importMarcAuthority(marc, force);
    if (result.isSuccess()) {
      return ResponseEntity.ok().body(Map.of(
        "id", result.success().getId(),
        "label", result.success().getLabel(),
        "doc", result.success().getDoc()
      ));
    } else {
      return ResponseEntity.badRequest().body(result.failure());
    }
  }
}
