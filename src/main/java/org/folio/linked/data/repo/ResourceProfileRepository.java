package org.folio.linked.data.repo;

import org.folio.linked.data.model.entity.ResourceProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceProfileRepository extends JpaRepository<ResourceProfile, Long> {
}
