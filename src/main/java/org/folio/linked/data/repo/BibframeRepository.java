package org.folio.linked.data.repo;

import java.util.Optional;
import org.folio.linked.data.model.entity.Bibframe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibframeRepository extends JpaRepository<Bibframe, Long> {

  Optional<Bibframe> findBySlug(String slug);

}
