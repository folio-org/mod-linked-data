package org.folio.linked.data.mapper.kafka.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.linked.data.util.Constants.SEARCH_AUTHORITY_RESOURCE_NAME;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.LinkedDataAuthority;
import org.folio.linked.data.domain.dto.LinkedDataIdentifier;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = SPRING, imports = UUID.class)
// We cannot use constructor injection in the subclass due to https://github.com/mapstruct/mapstruct/issues/2257
// so, we use field injection here.
@SuppressWarnings("java:S6813")
public abstract class AuthoritySearchMessageMapper {

  @Autowired
  protected IndexIdentifierMapper indexIdentifierMapper;

  @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
  @Mapping(target = "resourceName", constant = SEARCH_AUTHORITY_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataAuthority(resource))")
  public abstract ResourceIndexEvent toIndex(Resource resource);

  @Mapping(target = "type", source = "resource")
  @Mapping(target = "identifiers", source = "resource")
  protected abstract LinkedDataAuthority toLinkedDataAuthority(Resource resource);

  protected List<LinkedDataIdentifier> extractIdentifiers(Resource resource) {
    return indexIdentifierMapper.extractIdentifiers(resource);
  }

  protected String parseType(Resource resource) {
    if (resource.isOfType(CONCEPT)) {
      return CONCEPT.name();
    }
    return resource.getTypes()
      .stream()
      .findFirst()
      .map(ResourceTypeEntity::getUri)
      .flatMap(ResourceTypeDictionary::fromUri)
      .map(Enum::name)
      .orElse(StringUtils.EMPTY);
  }
}
