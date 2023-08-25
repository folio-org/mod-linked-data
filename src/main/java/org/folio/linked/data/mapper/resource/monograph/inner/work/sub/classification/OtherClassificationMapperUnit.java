package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.OTHER_CLASS;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationOther2;
import org.folio.linked.data.domain.dto.ClassificationOtherField2;
import org.folio.linked.data.domain.dto.Work2;
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
  public Work2 toDto(Resource source, Work2 destination) {
    var other = coreMapper.readResourceDoc(source, ClassificationOther2.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, other::addAssignerItem);
    destination.addClassificationItem(new ClassificationOtherField2().otherClassification(other));
    return destination;
  }
}
