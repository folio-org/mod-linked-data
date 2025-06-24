package org.folio.linked.data.service.profile;

import java.util.List;
import org.folio.linked.data.domain.dto.ProfileMetadata;

public interface ProfileService {
  void saveAllProfiles();

  String getProfile();

  String getProfileById(Integer id);

  List<ProfileMetadata> getMetadataByResourceType(String resourceTypeUri);
}
