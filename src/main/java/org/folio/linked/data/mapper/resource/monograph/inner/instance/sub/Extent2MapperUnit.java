package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO_URL;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.LABEL_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Extent2;
import org.folio.linked.data.domain.dto.ExtentField2;
import org.folio.linked.data.domain.dto.Instance2;
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
@MapperUnit(type = EXTENT_URL, predicate = EXTENT_PRED, dtoClass = ExtentField2.class)
public class Extent2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;
  private final NoteMapperUnit<Extent2> noteMapper;
  private final AppliesToMapperUnit<Extent2> appliesToMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var extent = coreMapper.readResourceDoc(source, Extent2.class);
    coreMapper.addMappedResources(appliesToMapper, source, APPLIES_TO_PRED, extent);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, extent);
    destination.addExtentItem(new ExtentField2().extent(extent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var extent = ((ExtentField2) dto).getExtent();
    var resource = new Resource();
    resource.setLabel(EXTENT_URL);
    resource.addType(resourceTypeService.get(EXTENT));
    resource.setDoc(getDoc(extent.getLabel()));
    coreMapper.mapResourceEdges(extent.getAppliesTo(), resource, APPLIES_TO_URL, APPLIES_TO_PRED,
      (fieldDto, pred) -> appliesToMapper.toEntity(fieldDto, pred, null));
    coreMapper.mapResourceEdges(extent.getNote(), resource, NOTE_URL, NOTE_PRED,
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
