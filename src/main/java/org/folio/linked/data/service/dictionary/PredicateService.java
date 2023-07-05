package org.folio.linked.data.service.dictionary;

import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.PREDICATE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.repo.PredicateRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PredicateService implements DictionaryService<Predicate> {

  private final PredicateRepository repo;

  @Override
  public Predicate get(String key) {
    return repo.findPredicateByLabel(key)
      .orElseThrow(() -> new NotSupportedException(PREDICATE + key + IS_NOT_SUPPORTED));

  }
}
