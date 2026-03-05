package org.folio.linked.data.repo;

import org.folio.linked.data.model.entity.VocabularyEntity;
import org.springframework.data.repository.CrudRepository;

public interface VocabularyRepository extends CrudRepository<VocabularyEntity, String> {
}
