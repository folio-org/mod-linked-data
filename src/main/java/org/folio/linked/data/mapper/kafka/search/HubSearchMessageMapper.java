package org.folio.linked.data.mapper.kafka.search;

import static org.folio.linked.data.util.Constants.SEARCH_HUB_RESOURCE_NAME;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.LinkedDataHub;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.model.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, imports = UUID.class)
public abstract class HubSearchMessageMapper {

  private static final Pattern HUB_LINK_PATTERN = Pattern.compile("/resources/hubs/([^./]+)");

  @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
  @Mapping(target = "resourceName", constant = SEARCH_HUB_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataHub(resource))")
  public abstract ResourceIndexEvent toIndex(Resource resource, ResourceIndexEventType type);

  @Mapping(target = "originalId", expression = "java(getOriginalId(resource))")
  protected abstract LinkedDataHub toLinkedDataHub(Resource resource);

  protected String getOriginalId(Resource hub) {
    return getPropertyValues(hub, PropertyDictionary.LINK).stream()
      .map(HUB_LINK_PATTERN::matcher)
      .filter(Matcher::find)
      .findFirst()
      .map(matcher -> matcher.group(1))
      .orElse(null);
  }
}
