package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_URI;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ImmediateAcquisition;
import org.folio.linked.data.domain.dto.ImmediateAcquisitionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = IMM_ACQUISITION, predicate = IMM_ACQUISITION_PRED, dtoClass = ImmediateAcquisitionField.class)
public class ImmediateAcquisitionMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var item = coreMapper.readResourceDoc(source, ImmediateAcquisition.class);
    coreMapper.addMappedProperties(source, APPLICABLE_INSTITUTION_PRED, item::addApplicableInstitutionItem);
    destination.addImmediateAcquisitionItem(new ImmediateAcquisitionField().immediateAcquisition(item));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var immediateAcquisition = ((ImmediateAcquisitionField) dto).getImmediateAcquisition();
    var resource = new Resource();
    resource.setLabel(IMM_ACQUISITION_URI);
    resource.setType(resourceTypeService.get(IMM_ACQUISITION));
    resource.setDoc(getDoc(immediateAcquisition));
    coreMapper.mapPropertyEdges(immediateAcquisition.getApplicableInstitution(), resource,
      APPLICABLE_INSTITUTION_PRED, APPLICABLE_INSTITUTION_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ImmediateAcquisition dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, dto.getLabel());
    return coreMapper.toJson(map);
  }
}
