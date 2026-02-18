package org.folio.linked.data.repo;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.model.entity.Profile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends CrudRepository<Profile, Integer> {
  @EntityGraph(attributePaths = {"resourceType", "additionalResourceTypes"})
  @Query("SELECT p FROM Profile p WHERE p.id = :id")
  Optional<Profile> findByIdWithAdditionalResourceTypes(@Param("id") Integer id);

  List<Profile> findByResourceTypeUriOrderByIdAsc(String resourceTypeUri);
}
