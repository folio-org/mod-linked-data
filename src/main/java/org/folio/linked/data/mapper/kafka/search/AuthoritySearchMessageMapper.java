package org.folio.linked.data.mapper.kafka.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.linked.data.util.Constants.SEARCH_AUTHORITY_RESOURCE_NAME;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeAuthorityIdentifiersInner;
import org.folio.search.domain.dto.LinkedDataAuthority;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = SPRING)
public abstract class AuthoritySearchMessageMapper {

  @Autowired
  protected IndexIdentifierMapper<BibframeAuthorityIdentifiersInner> innerIndexIdentifierMapper;

  @Mapping(target = "resourceName", constant = SEARCH_AUTHORITY_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataAuthority(resource))")
  public abstract ResourceIndexEvent toIndex(Resource resource);

  @Mapping(target = "type", source = "resource")
  @Mapping(target = "identifiers", source = "resource")
  protected abstract LinkedDataAuthority toLinkedDataAuthority(Resource resource);

  protected List<BibframeAuthorityIdentifiersInner> extractIdentifiers(Resource resource) {
    return innerIndexIdentifierMapper.extractIdentifiers(resource);
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
