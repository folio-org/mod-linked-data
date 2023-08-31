package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.Bibframe2Constants.WORK_TITLE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.domain.dto.WorkTitle2;
import org.folio.linked.data.domain.dto.WorkTitleField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK_TITLE)
public class WorkTitleMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var workTitle = coreMapper.readResourceDoc(source, WorkTitle2.class);
    destination.addTitleItem(new WorkTitleField2().workTitle(workTitle));
    return destination;
  }

}
