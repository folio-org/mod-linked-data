package org.folio.linked.data.repo;

import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.model.entity.Bibframe;

public interface BibframeRepo {

  Bibframe persist(Bibframe bibframe);

  Optional<Bibframe> read(UUID id);
}
