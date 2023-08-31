package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PARALLEL_TITLE_2;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ParallelTitle2;
import org.folio.linked.data.domain.dto.ParallelTitleField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PARALLEL_TITLE_2)
public class WorkParallelTitleMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final NoteMapperUnit<ParallelTitle2> noteMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var parallelTitle = coreMapper.readResourceDoc(source, ParallelTitle2.class);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, parallelTitle);
    destination.addTitleItem(new ParallelTitleField2().parallelTitle(parallelTitle));
    return destination;
  }

}
