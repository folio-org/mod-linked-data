package org.folio.linked.data.repo;

import java.util.List;
import org.folio.linked.data.model.entity.Profile;
import org.springframework.data.repository.CrudRepository;

public interface ProfileRepository extends CrudRepository<Profile, Integer> {
  List<Profile> findByResourceTypeUri(String resourceTypeUri);
}
