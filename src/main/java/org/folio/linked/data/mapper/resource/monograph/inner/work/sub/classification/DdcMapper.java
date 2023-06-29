package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.DDC;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationDdc;
import org.folio.linked.data.domain.dto.ClassificationDdcField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = DDC)
public class DdcMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var ddc = commonMapper.readResourceDoc(source, ClassificationDdc.class);
    commonMapper.addMappedProperties(source, ASSIGNER_PRED, ddc::addAssignerItem);
    commonMapper.addMappedProperties(source, SOURCE_PRED, ddc::addSourceItem);
    destination.addClassificationItem(new ClassificationDdcField().classificationDdc(ddc));
    return destination;
  }

}
