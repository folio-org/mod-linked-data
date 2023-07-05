package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.content;

import static org.folio.linked.data.util.BibframeConstants.TABLE_OF_CONTENTS_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = TABLE_OF_CONTENTS_PRED)
public class TableOfContentsMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var property = coreMapper.toProperty(source);
    destination.addTableOfContentsItem(property);
    return destination;
  }
}
