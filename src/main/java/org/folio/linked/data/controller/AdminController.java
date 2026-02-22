package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.AdminApi;
import org.folio.linked.data.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

  private final CacheService cacheService;

  @Override
  public ResponseEntity<Void> clearCaches() {
    cacheService.clearCaches();
    return ResponseEntity.noContent().build();
  }
}
