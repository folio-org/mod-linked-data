package org.folio.linked.data.repo;

import java.util.List;
import java.util.UUID;
import org.folio.linked.data.model.entity.PreferredProfile;
import org.folio.linked.data.model.entity.pk.PreferredProfilePk;
import org.springframework.data.repository.CrudRepository;

public interface PreferredProfileRepository extends CrudRepository<PreferredProfile, PreferredProfilePk> {
  List<PreferredProfile> findByIdUserId(UUID userId);
}
