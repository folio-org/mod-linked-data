package org.folio.linked.data.repo;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.model.entity.Bibframe;
import org.springframework.stereotype.Repository;

@Repository
public class BibframeRepoImpl implements BibframeRepo {

  private Map<UUID, Bibframe> tempStorage;

  @Override
  public Bibframe persist(Bibframe bibframe) {
    if (isNull(bibframe.getId())) {
      bibframe.setId(UUID.randomUUID());
    }
    tempStorage.put(bibframe.getId(), bibframe);
    return bibframe;
  }

  @Override
  public Optional<Bibframe> read(UUID id) {
    return Optional.ofNullable(tempStorage.get(id));
  }
}
