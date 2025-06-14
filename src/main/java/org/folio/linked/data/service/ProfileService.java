package org.folio.linked.data.service;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Profile;

public interface ProfileService {

  Profile saveProfile(Integer id, String name, String resourceTypeUri, String value);

  String getProfile();

  String getProfileById(Integer id);

  List<ProfileMetadata> getMetadataByResourceType(String resourceTypeUri);

  void setPreferredProfile(UUID userId, Integer profileId, String resourceTypeUri);

  List<ProfileMetadata> getPreferredProfiles(UUID userId, @Nullable String resourceTypeUri);
}
