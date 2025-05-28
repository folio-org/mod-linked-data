package org.folio.linked.data.repo;

import java.util.List;
import org.folio.linked.data.model.entity.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends CrudRepository<Profile, Long> {
  @Query("SELECT p FROM Profile p WHERE p.resourceType.uri = :resourceTypeUri")
  List<Profile> findByResourceTypeUri(@Param("resourceTypeUri") String resourceTypeUri);
}
