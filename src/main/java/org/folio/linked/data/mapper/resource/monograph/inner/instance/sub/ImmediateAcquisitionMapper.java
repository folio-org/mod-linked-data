package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_URI;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ImmediateAcquisition;
import org.folio.linked.data.domain.dto.ImmediateAcquisitionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = IMM_ACQUISITION_PRED, dtoClass = ImmediateAcquisitionField.class)
public class ImmediateAcquisitionMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var item = commonMapper.readResourceDoc(source, ImmediateAcquisition.class);
    commonMapper.addMappedProperties(source, APPLICABLE_INSTITUTION_PRED, item::addApplicableInstitutionItem);
    destination.addImmediateAcquisitionItem(new ImmediateAcquisitionField().immediateAcquisition(item));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var immediateAcquisition = ((ImmediateAcquisitionField) dto).getImmediateAcquisition();
    var resource = new Resource();
    resource.setLabel(IMM_ACQUISITION_URI);
    resource.setType(resourceTypeService.get(IMM_ACQUISITION_URI));
    resource.setDoc(getDoc(immediateAcquisition));
    commonMapper.mapPropertyEdges(immediateAcquisition.getApplicableInstitution(), resource,
      APPLICABLE_INSTITUTION_PRED, APPLICABLE_INSTITUTION_URL);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ImmediateAcquisition dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PROPERTY_LABEL, dto.getLabel());
    return commonMapper.toJson(map);
  }
}
