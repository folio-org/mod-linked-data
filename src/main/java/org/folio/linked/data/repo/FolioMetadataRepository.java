package org.folio.linked.data.repo;

import java.util.Optional;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolioMetadataRepository extends JpaRepository<FolioMetadata, Long> {

  Optional<IdOnly> findIdByInventoryId(String inventoryId);

  Optional<FolioMetadata> findBySrsId(String srsId);

  boolean existsBySrsId(String srsId);

  Boolean existsByInventoryId(String inventoryId);

  interface IdOnly {
    Long getId();
  }
}
