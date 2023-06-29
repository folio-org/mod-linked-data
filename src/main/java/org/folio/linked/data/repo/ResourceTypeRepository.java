package org.folio.linked.data.repo;

import static org.folio.linked.data.util.Constants.CONDITION_TO_CACHE;

import java.util.Optional;
import org.folio.linked.data.model.entity.ResourceType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

  @Cacheable(value = "resourceTypes", condition = CONDITION_TO_CACHE)
  Optional<ResourceType> findBySimpleLabel(String simpleLabel);

  @Cacheable(value = "resourceTypes", condition = CONDITION_TO_CACHE)
  Optional<ResourceType> findByTypeUri(String typeUri);
}
