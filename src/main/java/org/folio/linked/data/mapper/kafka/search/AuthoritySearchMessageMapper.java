package org.folio.linked.data.mapper.kafka.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.linked.data.util.Constants.SEARCH_AUTHORITY_RESOURCE_NAME;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.UUID;
import org.folio.linked.data.domain.dto.LinkedDataAuthority;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = SPRING, imports = UUID.class)
public abstract class AuthoritySearchMessageMapper {

  @Autowired
  protected IndexIdentifierMapper indexIdentifierMapper;

  @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
  @Mapping(target = "resourceName", constant = SEARCH_AUTHORITY_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataAuthority(resource))")
  public abstract ResourceIndexEvent toIndex(Resource resource, ResourceIndexEventType type);

  @Mapping(target = "type", expression = "java(extractType(resource))")
  @Mapping(target = "identifiers", expression = "java(indexIdentifierMapper.extractIdentifiers(resource))")
  protected abstract LinkedDataAuthority toLinkedDataAuthority(Resource resource);

  protected String extractType(Resource resource) {
    return resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .filter(typeUri -> !CONCEPT.getUri().equals(typeUri))
      .findFirst()
      .orElse(null);
  }
}
