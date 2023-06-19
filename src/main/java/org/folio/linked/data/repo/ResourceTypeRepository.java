package org.folio.linked.data.repo;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.model.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

  Optional<ResourceType> findBySimpleLabel(String simpleLabel);

  List<ResourceType> findByTypeUri(String typeUri);
}
