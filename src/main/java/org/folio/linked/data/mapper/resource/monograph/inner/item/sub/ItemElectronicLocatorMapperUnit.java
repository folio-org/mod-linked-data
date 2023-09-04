package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.Bibframe2Constants.ELECTRONIC_LOCATOR_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.domain.dto.Url2;
import org.folio.linked.data.domain.dto.UrlField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = ELECTRONIC_LOCATOR_2_PRED)
public class ItemElectronicLocatorMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final NoteMapperUnit<Url2> noteMapper;

  @Override
  public Item2 toDto(Resource source, Item2 destination) {
    var url = coreMapper.toUrl(source);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, url);
    destination.addElectronicLocatorItem(new UrlField2().url(url));
    return destination;
  }
}
