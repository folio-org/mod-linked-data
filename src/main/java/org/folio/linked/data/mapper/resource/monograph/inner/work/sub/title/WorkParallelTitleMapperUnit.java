package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PARALLEL_TITLE)
public class WorkParallelTitleMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final NoteMapperUnit<ParallelTitle> noteMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var parallelTitle = coreMapper.readResourceDoc(source, ParallelTitle.class);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, parallelTitle);
    destination.addTitleItem(new ParallelTitleField().parallelTitle(parallelTitle));
    return destination;
  }

}
