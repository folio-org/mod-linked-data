package org.folio.linked.data.mapper.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.IMMEDIATE_ACQUISITION_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ImmediateAcquisition;
import org.folio.linked.data.domain.dto.ImmediateAcquisitionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(IMMEDIATE_ACQUISITION_PRED)
public class ImmediateAcquisitionMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var item = readResourceDoc(objectMapper, source, ImmediateAcquisition.class);
    addMappedProperties(objectMapper, source, APPLICABLE_INSTITUTION_PRED, item::addApplicableInstitutionItem);
    destination.addImmediateAcquisitionItem(new ImmediateAcquisitionField().immediateAcquisition(item));
    return destination;
  }
}
