package org.folio.linked.data.mapper.dictionary;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
public interface PredicateMapper {

  PredicateEntity toEntity(PredicateDictionary dictionary);

  @BeforeMapping
  default void validatePredicateDictionary(PredicateDictionary dictionary, @MappingTarget PredicateEntity entity) {
    if (dictionary == PredicateDictionary.NULL) {
      throw new IllegalArgumentException("This value of PredicateDictionary is not supported");
    }
  }
}
