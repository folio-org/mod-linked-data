package org.folio.linked.data.repo;

import java.util.Optional;
import org.folio.linked.data.model.entity.ResourceType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

  @Cacheable("resourceTypesByLabel")
  Optional<ResourceType> findBySimpleLabel(String simpleLabel);

  @Cacheable("resourceTypesByUri")
  Optional<ResourceType> findByTypeUri(String typeUri);
}
