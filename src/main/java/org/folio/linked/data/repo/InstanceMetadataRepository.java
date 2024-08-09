package org.folio.linked.data.repo;

import java.util.Optional;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceMetadataRepository extends JpaRepository<InstanceMetadata, Long> {

  Optional<IdOnly> findIdByInventoryId(String inventoryId);

  Optional<InstanceMetadata> findByInventoryId(String inventoryId);

  interface IdOnly {
    Long getId();
  }
}
