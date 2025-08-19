package org.folio.linked.data.service.profile;

import java.util.List;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Profile;

public interface ProfileService {
  void saveAllProfiles();

  Profile getProfileById(Integer id);

  List<ProfileMetadata> getMetadataByResourceType(String resourceTypeUri);
}
