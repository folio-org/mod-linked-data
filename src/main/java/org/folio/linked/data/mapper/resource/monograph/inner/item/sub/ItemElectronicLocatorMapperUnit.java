package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.domain.dto.UrlField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = ELECTRONIC_LOCATOR_PRED)
public class ItemElectronicLocatorMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final NoteMapperUnit<Url> noteMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var url = coreMapper.toUrl(source);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, url);
    destination.addElectronicLocatorItem(new UrlField().url(url));
    return destination;
  }
}
