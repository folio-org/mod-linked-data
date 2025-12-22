package org.folio.linked.data.service;

import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.values;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.mapper.dictionary.PredicateMapper;
import org.folio.linked.data.mapper.dictionary.ResourceTypeMapper;
import org.folio.linked.data.repo.PredicateRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

  private final PredicateRepository predicateRepository;
  private final ResourceTypeRepository resourceTypeRepository;
  private final PredicateMapper predicateMapper;
  private final ResourceTypeMapper resourceTypeMapper;

  @Transactional
  @Override
  public void init() {
    initPredicates();
    initTypes();
  }

  private void initPredicates() {
    var predicates = Arrays.stream(PredicateDictionary.values())
      .filter(predicate -> ObjectUtils.notEqual(predicate, PredicateDictionary.NULL))
      .map(predicateMapper::toEntity)
      .toList();
    predicateRepository.saveAll(predicates);
  }

  private void initTypes() {
    var types = Arrays.stream(values())
      .filter(t -> t != MOCKED_RESOURCE)
      .map(resourceTypeMapper::toEntity)
      .toList();
    resourceTypeRepository.saveAll(types);
  }
}
