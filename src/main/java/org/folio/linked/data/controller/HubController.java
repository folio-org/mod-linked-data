package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.rest.resource.HubApi;
import org.folio.linked.data.service.hub.HubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HubController implements HubApi {

  private final HubService hubService;

  @Override
  public ResponseEntity<ResourceResponseDto> getHubPreviewByUri(String hubUri) {
    return ResponseEntity.ok(hubService.previewHub(hubUri));
  }

  @Override
  public ResponseEntity<ResourceResponseDto> saveHubByUri(String hubUri) {
    return ResponseEntity.ok(hubService.saveHub(hubUri));
  }
}
