package org.folio.linked.data.repo;

import org.folio.linked.data.model.entity.Profile;
import org.springframework.data.repository.CrudRepository;

public interface ProfileRepository extends CrudRepository<Profile, Integer> {
}
