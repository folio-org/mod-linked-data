package org.folio.linked.data.mapper.kafka.impl;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.mapper.kafka.IndexIdentifierMapper;
import org.folio.linked.data.mapper.kafka.KafkaSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeAuthorityIdentifiersInner;
import org.folio.search.domain.dto.BibframeAuthorityIndex;
import org.folio.search.domain.dto.ResourceIndexEventType;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuthoritySearchMessageMapper implements KafkaSearchMessageMapper<BibframeAuthorityIndex> {

  private final IndexIdentifierMapper<BibframeAuthorityIdentifiersInner> innerIndexIdentifierMapper;

  @Override
  public Optional<Long> toDeleteIndexId(@NonNull Resource resource) {
    log.debug("Delete index for authority isn't supported");
    return Optional.empty();
  }

  @Override
  public Optional<BibframeAuthorityIndex> toIndex(Resource resource, ResourceIndexEventType eventType) {
    var indexDto = new BibframeAuthorityIndex()
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
      return CONCEPT.toString();
    }
    return resource.getTypes()
      .stream()
      .findFirst()
      .map(Object::toString)
      .orElse(StringUtils.EMPTY);
  }
}
