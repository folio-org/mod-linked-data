package org.folio.linked.data.mapper.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.DDC;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationDdc;
import org.folio.linked.data.domain.dto.ClassificationDdcField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(DDC)
public class DdcMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var ddc = readResourceDoc(objectMapper, source, ClassificationDdc.class);
    addMappedProperties(objectMapper, source, ASSIGNER_PRED, ddc::addAssignerItem);
    addMappedProperties(objectMapper, source, SOURCE_PRED, ddc::addSourceItem);
    destination.addClassificationItem(new ClassificationDdcField().classificationDdc(ddc));
    return destination;
  }

}
