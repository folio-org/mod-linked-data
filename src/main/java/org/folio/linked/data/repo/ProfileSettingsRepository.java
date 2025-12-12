package org.folio.linked.data.repo;

import java.util.List;
import java.util.UUID;
import org.folio.linked.data.domain.dto.ProfileSettings;
import org.folio.linked.data.model.entity.pk.ProfileSettingsPk;
import org.springframework.data.repository.CrudRepository;

public interface ProfileSettingsRepository extends CrudRepository<ProfileSettings, ProfileSettingsPk> {
  List<ProfileSettings> findByIdUserIdProfileId(UUID userId, Integer profileId);
}
