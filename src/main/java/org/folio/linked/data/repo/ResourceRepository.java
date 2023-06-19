package org.folio.linked.data.repo;

import org.folio.linked.data.model.ResourceHashAndProfile;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

  Page<ResourceHashAndProfile> findAllBy(Pageable pageable);

}
