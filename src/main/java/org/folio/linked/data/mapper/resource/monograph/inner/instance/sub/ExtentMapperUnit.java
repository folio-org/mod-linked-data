package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO_URL;
import static org.folio.linked.data.util.BibframeConstants.EXTENT;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AppliesTo;
import org.folio.linked.data.domain.dto.AppliesToField;
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.domain.dto.ExtentField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = EXTENT, predicate = EXTENT_PRED, dtoClass = ExtentField.class)
public class ExtentMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;
  private final InstanceNoteMapperUnit noteMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var extent = coreMapper.readResourceDoc(source, Extent.class);
    addMappedAppliesTo(source, extent);
    coreMapper.addMappedProperties(source, NOTE_PRED, extent::addNoteItem);
    destination.addExtentItem(new ExtentField().extent(extent));
    return destination;
  }

  private void addMappedAppliesTo(Resource resource, Extent extent) {
    resource.getOutgoingEdges().stream()
      .filter(resourceEdge -> APPLIES_TO_PRED.equals(resourceEdge.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(r -> coreMapper.readResourceDoc(r, AppliesTo.class))
      .forEach(at -> extent.addAppliesToItem(new AppliesToField().appliesTo(at)));
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var extent = ((ExtentField) dto).getExtent();
    var resource = new Resource();
    resource.setLabel(EXTENT_URL);
    resource.setType(resourceTypeService.get(EXTENT));
    resource.setDoc(getDoc(extent.getLabel()));
    coreMapper.mapResourceEdges(extent.getAppliesTo(), resource, APPLIES_TO, APPLIES_TO_PRED, this::appliesToToEntity);
    coreMapper.mapResourceEdges(extent.getNote(), resource, null, NOTE_PRED, noteMapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private Resource appliesToToEntity(AppliesToField dto, String predicate) {
    var resource = new Resource();
    resource.setLabel(APPLIES_TO_URL);
    resource.setType(resourceTypeService.get(APPLIES_TO));
    resource.setDoc(getDoc(dto.getAppliesTo().getLabel()));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(List<String> labelsDto) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, labelsDto);
    return coreMapper.toJson(map);
  }
}
