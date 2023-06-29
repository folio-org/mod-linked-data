package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.OTHER_CLASS;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationOther;
import org.folio.linked.data.domain.dto.ClassificationOtherField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = OTHER_CLASS)
public class OtherClassificationMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var other = commonMapper.readResourceDoc(source, ClassificationOther.class);
    commonMapper.addMappedProperties(source, ASSIGNER_PRED, other::addAssignerItem);
    destination.addClassificationItem(new ClassificationOtherField().otherClassification(other));
    return destination;
  }
}
