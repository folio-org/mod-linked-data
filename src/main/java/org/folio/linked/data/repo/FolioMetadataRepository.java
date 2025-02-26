package org.folio.linked.data.repo;

import java.util.Optional;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolioMetadataRepository extends JpaRepository<FolioMetadata, Long> {

  Optional<IdOnly> findIdByInventoryId(String inventoryId);

  Optional<IdOnly> findIdBySrsId(String srsId);

  boolean existsBySrsId(String srsId);

  Optional<FolioMetadata> findByInventoryId(String inventoryId);

  Optional<InventoryIdOnly> findInventoryIdById(Long id);

  interface IdOnly {
    Long getId();
  }

  interface InventoryIdOnly {
    String getInventoryId();
  }
}
