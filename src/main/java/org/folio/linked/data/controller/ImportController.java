package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.rest.resource.ImportApi;
import org.folio.linked.data.service.rdf.RdfImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImportController implements ImportApi {
  private final RdfImportService rdfImportService;

  @Override
  public ResponseEntity<ImportFileResponseDto> importFile(MultipartFile multipartFile) {
    return ResponseEntity.ok(rdfImportService.importFile(multipartFile));
  }
}
