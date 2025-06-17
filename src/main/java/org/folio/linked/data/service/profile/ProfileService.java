package org.folio.linked.data.service.profile;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.folio.linked.data.domain.dto.ProfileMetadata;

public interface ProfileService {
  void saveAllProfiles();

  String getProfile();

  String getProfileById(Integer id);

  List<ProfileMetadata> getMetadataByResourceType(String resourceTypeUri);

  void setPreferredProfile(UUID userId, Integer profileId, String resourceTypeUri);

  List<ProfileMetadata> getPreferredProfiles(UUID userId, @Nullable String resourceTypeUri);
}
