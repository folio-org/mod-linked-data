package org.folio.linked.data.repo;

import java.util.List;
import java.util.UUID;
import org.folio.linked.data.model.entity.PreferredProfileSettings;
import org.folio.linked.data.model.entity.pk.PreferredProfileSettingsPk;
import org.springframework.data.repository.CrudRepository;

public interface PreferredProfileSettingsRepository
    extends CrudRepository<PreferredProfileSettings, PreferredProfileSettingsPk> {
  List<PreferredProfileSettings> findByIdUserIdAndIdProfileId(UUID userId, Integer profileId);
}
