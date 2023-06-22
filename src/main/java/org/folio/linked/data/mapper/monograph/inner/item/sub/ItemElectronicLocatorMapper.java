package org.folio.linked.data.mapper.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.toUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.UrlField;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(ELECTRONIC_LOCATOR_PRED)
public class ItemElectronicLocatorMapper implements ItemSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var url = toUrl(objectMapper, source);
    addMappedProperties(objectMapper, source, NOTE_PRED, url::addNoteItem);
    destination.addElectronicLocatorItem(new UrlField().url(url));
    return destination;
  }
}
