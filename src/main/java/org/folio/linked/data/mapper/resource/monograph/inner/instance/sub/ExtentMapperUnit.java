package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
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
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.AppliesToMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
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
  private final NoteMapperUnit<Extent> noteMapper;
  private final AppliesToMapperUnit<Extent> appliesToMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var extent = coreMapper.readResourceDoc(source, Extent.class);
    coreMapper.addMappedResources(appliesToMapper, source, APPLIES_TO_PRED, extent);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, extent);
    destination.addExtentItem(new ExtentField().extent(extent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var extent = ((ExtentField) dto).getExtent();
    var resource = new Resource();
    resource.setLabel(EXTENT_URL);
    resource.setType(resourceTypeService.get(EXTENT));
    resource.setDoc(getDoc(extent.getLabel()));
    coreMapper.mapResourceEdges(extent.getAppliesTo(), resource, APPLIES_TO, APPLIES_TO_PRED,
      (fieldDto, pred) -> appliesToMapper.toEntity(fieldDto, pred, null));
    coreMapper.mapResourceEdges(extent.getNote(), resource, NOTE, NOTE_PRED,
      (fieldDto, pred) -> noteMapper.toEntity(fieldDto, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(List<String> labels) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, labels);
    return coreMapper.toJson(map);
  }
}
