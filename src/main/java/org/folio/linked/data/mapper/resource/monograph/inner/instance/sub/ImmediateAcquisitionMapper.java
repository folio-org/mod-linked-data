package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_URI;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.hash;
import static org.folio.linked.data.util.MappingUtil.mapPropertyEdges;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;
import static org.folio.linked.data.util.MappingUtil.toJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ImmediateAcquisition;
import org.folio.linked.data.domain.dto.ImmediateAcquisitionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = IMM_ACQUISITION_PRED, dtoClass = ImmediateAcquisitionField.class)
public class ImmediateAcquisitionMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var item = readResourceDoc(mapper, source, ImmediateAcquisition.class);
    addMappedProperties(mapper, source, APPLICABLE_INSTITUTION_PRED, item::addApplicableInstitutionItem);
    destination.addImmediateAcquisitionItem(new ImmediateAcquisitionField().immediateAcquisition(item));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var immediateAcquisition = ((ImmediateAcquisitionField) dto).getImmediateAcquisition();
    var resource = new Resource();
    resource.setLabel(IMM_ACQUISITION_URI);
    resource.setType(resourceTypeService.get(IMM_ACQUISITION_URI));
    resource.setDoc(toJson(getDoc(immediateAcquisition), mapper));
    mapPropertyEdges(immediateAcquisition.getApplicableInstitution(), resource,
      () -> predicateService.get(APPLICABLE_INSTITUTION_PRED), () -> resourceTypeService.get(APPLICABLE_INSTITUTION_URL),
      mapper);
    resource.setResourceHash(hash(resource, mapper));
    return resource;
  }

  private Map<String, List<String>> getDoc(ImmediateAcquisition dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PROPERTY_LABEL, dto.getLabel());
    return map;
  }
}
