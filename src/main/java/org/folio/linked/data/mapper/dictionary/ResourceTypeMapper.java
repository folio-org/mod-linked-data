package org.folio.linked.data.mapper.dictionary;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface ResourceTypeMapper {

  ResourceTypeEntity toEntity(ResourceTypeDictionary dictionary);

}
