package org.folio.linked.data.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.model.entity.ProfileSettings;
import org.springframework.data.repository.CrudRepository;

public interface ProfileSettingsRepository extends CrudRepository<ProfileSettings, Integer> {
  List<ProfileSettings> findByUserIdAndProfileId(UUID userId, Integer profileId);

  Optional<ProfileSettings> findByIdAndUserId(Integer id, UUID userId);

  void deleteByIdAndProfileIdAndUserId(Integer id, Integer profileId, UUID userId);
}
