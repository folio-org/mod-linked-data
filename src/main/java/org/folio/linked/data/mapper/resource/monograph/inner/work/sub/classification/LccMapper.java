package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.LCC;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationLcc;
import org.folio.linked.data.domain.dto.ClassificationLccField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = LCC)
public class LccMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var lcc = commonMapper.readResourceDoc(source, ClassificationLcc.class);
    commonMapper.addMappedProperties(source, ASSIGNER_PRED, lcc::addAssignerItem);
    commonMapper.addMappedProperties(source, STATUS_PRED, lcc::addStatusItem);
    destination.addClassificationItem(new ClassificationLccField().classificationLcc(lcc));
    return destination;
  }

}
