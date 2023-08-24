package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.DDC;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationDdc2;
import org.folio.linked.data.domain.dto.ClassificationDdcField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = DDC)
public class DdcMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var ddc = coreMapper.readResourceDoc(source, ClassificationDdc2.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, ddc::addAssignerItem);
    coreMapper.addMappedProperties(source, SOURCE_PRED, ddc::addSourceItem);
    destination.addClassificationItem(new ClassificationDdcField2().classificationDdc(ddc));
    return destination;
  }

}
