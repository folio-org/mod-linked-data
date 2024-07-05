package org.folio.linked.data.mapper.kafka.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeAuthorityIdentifiersInner;
import org.folio.search.domain.dto.LinkedDataAuthority;
import org.folio.search.domain.dto.ResourceIndexEventType;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuthoritySearchMessageMapper implements KafkaSearchMessageMapper<LinkedDataAuthority> {

  private final IndexIdentifierMapper<BibframeAuthorityIdentifiersInner> innerIndexIdentifierMapper;

  @Override
  public Optional<Long> toDeleteIndexId(@NonNull Resource resource) {
    log.debug("Delete index for authority isn't supported");
    return Optional.empty();
  }

  @Override
  public Optional<LinkedDataAuthority> toIndex(Resource resource, ResourceIndexEventType eventType) {
    var indexDto = new LinkedDataAuthority()
      .id(parseId(resource))
      .label(resource.getLabel())
      .type(parseType(resource))
      .identifiers(innerIndexIdentifierMapper.extractIdentifiers(resource));
    return Optional.of(indexDto);
  }

  private String parseId(Resource resource) {
    return String.valueOf(resource.getId());
  }

  private String parseType(Resource resource) {
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
