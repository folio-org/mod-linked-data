package org.folio.linked.data.repo;

import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.model.entity.ProfileSettings;
import org.folio.linked.data.model.entity.pk.ProfileSettingsPk;
import org.springframework.data.repository.CrudRepository;

public interface ProfileSettingsRepository extends CrudRepository<ProfileSettings, ProfileSettingsPk> {
  Optional<ProfileSettings> getByIdUserIdProfileId(UUID userId, Integer profileId);
}
