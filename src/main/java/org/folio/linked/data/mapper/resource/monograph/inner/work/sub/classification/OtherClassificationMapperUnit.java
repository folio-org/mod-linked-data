package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.OTHER_CLASS;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationOther;
import org.folio.linked.data.domain.dto.ClassificationOtherField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = OTHER_CLASS)
public class OtherClassificationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var other = coreMapper.readResourceDoc(source, ClassificationOther.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, other::addAssignerItem);
    destination.addClassificationItem(new ClassificationOtherField().otherClassification(other));
    return destination;
  }
}
