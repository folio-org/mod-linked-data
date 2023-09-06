package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.LCC;
import static org.folio.linked.data.util.Bibframe2Constants.STATUS2_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationLcc2;
import org.folio.linked.data.domain.dto.ClassificationLccField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = LCC)
public class LccMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var lcc = coreMapper.readResourceDoc(source, ClassificationLcc2.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, lcc::addAssignerItem);
    coreMapper.addMappedProperties(source, STATUS2_PRED, lcc::addStatusItem);
    destination.addClassificationItem(new ClassificationLccField2().classificationLcc(lcc));
    return destination;
  }

}
