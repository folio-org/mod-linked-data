package org.folio.linked.data.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ProfileMetadata;
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

  @Override
  public ResponseEntity<String> getProfileById(Long profileId) {
    return ResponseEntity.ok(profileService.getProfileById(profileId));
  }

  @Override
  public ResponseEntity<List<ProfileMetadata>> getProfileMetadataByResourceType(String resourceTypeUri) {
    return ResponseEntity.ok(profileService.getMetadataByResourceType(resourceTypeUri));
  }

  @Override
  public ResponseEntity<Void> setPreferredProfile(UUID userId, ProfileMetadata profileMetadata) {
    profileService.setPreferredProfile(userId, profileMetadata.getId(), profileMetadata.getResourceType());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<ProfileMetadata>> getPreferredProfileByResourceType(UUID userId, String resourceTypeUri) {
    return ResponseEntity.ok(profileService.getPreferredProfile(userId, resourceTypeUri));
  }
}
