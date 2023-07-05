package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.LCC;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationLcc;
import org.folio.linked.data.domain.dto.ClassificationLccField;
import org.folio.linked.data.domain.dto.Work;
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
  public Work toDto(Resource source, Work destination) {
    var lcc = coreMapper.readResourceDoc(source, ClassificationLcc.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, lcc::addAssignerItem);
    coreMapper.addMappedProperties(source, STATUS_PRED, lcc::addStatusItem);
    destination.addClassificationItem(new ClassificationLccField().classificationLcc(lcc));
    return destination;
  }

}
