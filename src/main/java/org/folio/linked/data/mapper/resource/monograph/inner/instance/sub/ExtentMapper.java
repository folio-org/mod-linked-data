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
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = EXTENT, predicate = EXTENT_PRED, dtoClass = ExtentField.class)
public class ExtentMapper implements InstanceSubResourceMapper {

  private final CommonMapper commonMapper;
  private final DictionaryService<ResourceType> resourceTypeService;
  private final InstanceNoteMapper noteMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var extent = commonMapper.readResourceDoc(source, Extent.class);
    commonMapper.addMappedProperties(source, APPLIES_TO, extent::addAppliesToItem);
    commonMapper.addMappedProperties(source, NOTE_PRED, extent::addNoteItem);
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
    commonMapper.mapPropertyEdges(extent.getAppliesTo(), resource, APPLIES_TO, APPLIES_TO_URL);
    commonMapper.mapResourceEdges(extent.getNote(), resource, NOTE_PRED, noteMapper::toEntity);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Extent dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, dto.getLabel());
    return commonMapper.toJson(map);
  }
}
