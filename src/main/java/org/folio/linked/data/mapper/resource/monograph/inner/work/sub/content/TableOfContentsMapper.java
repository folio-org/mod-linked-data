package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.content;

import static org.folio.linked.data.util.BibframeConstants.TABLE_OF_CONTENTS_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = TABLE_OF_CONTENTS_PRED)
public class TableOfContentsMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var property = commonMapper.toProperty(source);
    destination.addTableOfContentsItem(property);
    return destination;
  }
}
