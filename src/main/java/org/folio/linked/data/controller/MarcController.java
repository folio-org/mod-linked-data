package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.MarcApi;
import org.folio.linked.data.service.MarcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MarcController implements MarcApi {

  private final MarcService marcService;

  @Override
  public ResponseEntity<String> isSupportedByInventoryId(String inventoryId) {
    return ResponseEntity.ok(marcService.isSupportedByInventoryId(inventoryId).toString());
  }
}
