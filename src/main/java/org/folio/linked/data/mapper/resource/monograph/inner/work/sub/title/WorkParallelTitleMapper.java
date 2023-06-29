package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = PARALLEL_TITLE)
public class WorkParallelTitleMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var parallelTitle = commonMapper.readResourceDoc(source, ParallelTitle.class);
    commonMapper.addMappedProperties(source, NOTE_PRED, parallelTitle::addNoteItem);
    destination.addTitleItem(new ParallelTitleField().parallelTitle(parallelTitle));
    return destination;
  }

}
