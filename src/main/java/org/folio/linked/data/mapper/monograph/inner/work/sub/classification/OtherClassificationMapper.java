package org.folio.linked.data.mapper.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.OTHER_CLASS;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationOther;
import org.folio.linked.data.domain.dto.ClassificationOtherField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(OTHER_CLASS)
public class OtherClassificationMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var other = readResourceDoc(objectMapper, source, ClassificationOther.class);
    addMappedProperties(objectMapper, source, ASSIGNER_PRED, other::addAssignerItem);
    destination.addClassificationItem(new ClassificationOtherField().otherClassification(other));
    return destination;
  }
}
