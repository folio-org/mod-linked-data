package org.folio.linked.data.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.PreferredProfileRequest;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.rest.resource.ProfileApi;
import org.folio.linked.data.service.profile.PreferredProfileService;
import org.folio.linked.data.service.profile.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {

  private final ProfileService profileService;
  private final PreferredProfileService preferredProfileService;

  @Override
  public ResponseEntity<String> getProfileById(Integer profileId) {
    return ResponseEntity.ok(profileService.getProfileById(profileId).getValue());
  }

  @Override
  public ResponseEntity<List<ProfileMetadata>> getProfileMetadataByResourceType(String resourceTypeUri) {
    return ResponseEntity.ok(profileService.getMetadataByResourceType(resourceTypeUri));
  }

  @Override
  public ResponseEntity<Void> setPreferredProfile(PreferredProfileRequest preferredProfile) {
    preferredProfileService.setPreferredProfile(preferredProfile.getId(), preferredProfile.getResourceType());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<ProfileMetadata>> getPreferredProfileByResourceType(String resourceTypeUri) {
    return ResponseEntity.ok(preferredProfileService.getPreferredProfiles(resourceTypeUri));
  }

  @Override
  public ResponseEntity<Void> deletePreferredProfile(String resourceTypeUri) {
    preferredProfileService.deletePreferredProfile(resourceTypeUri);
    return ResponseEntity.noContent().build();
  }
}
