package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.rest.resource.ProfileApi;
import org.folio.linked.data.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {

  private final ProfileService profileService;

  @Override
  public ResponseEntity<String> getProfile() {
    return ResponseEntity.ok(profileService.getProfile());
  }
}
