package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.ImportApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImportController implements ImportApi {
  @Override
  public ResponseEntity<String> importFile(MultipartFile fileName) {
    // This is an incomplete implementation, intended to be filled out later.
    return ResponseEntity.ok("imported");
  }
}
