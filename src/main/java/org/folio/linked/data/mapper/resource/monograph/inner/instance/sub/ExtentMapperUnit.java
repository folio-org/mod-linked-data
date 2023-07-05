package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO;
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
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.domain.dto.ExtentField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
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
    coreMapper.addMappedProperties(source, APPLIES_TO, extent::addAppliesToItem);
    coreMapper.addMappedProperties(source, NOTE_PRED, extent::addNoteItem);
    destination.addExtentItem(new ExtentField().extent(extent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var extent = ((ExtentField) dto).getExtent();
    var resource = new Resource();
    resource.setLabel(EXTENT_URL);
    resource.setType(resourceTypeService.get(EXTENT));
    resource.setDoc(getDoc(extent));
    coreMapper.mapPropertyEdges(extent.getAppliesTo(), resource, APPLIES_TO, APPLIES_TO_URL);
    coreMapper.mapResourceEdges(extent.getNote(), resource, NOTE_PRED, noteMapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Extent dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, dto.getLabel());
    return coreMapper.toJson(map);
  }
}
