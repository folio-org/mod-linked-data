package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.E_LOCATOR;
import static org.folio.linked.data.util.BibframeConstants.E_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.NOTE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ElectronicLocatorField;
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
@MapperUnit(type = E_LOCATOR, predicate = E_LOCATOR_PRED, dtoClass = ElectronicLocatorField.class)
public class InstanceElectronicLocatorMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var electronicLocator = coreMapper.readResourceDoc(source, ElectronicLocatorField.class);
    destination.addElectronicLocatorItem(electronicLocator);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var electronicLocator = (ElectronicLocatorField) dto;
    var resource = new Resource();
    resource.setLabel(getFirst(electronicLocator.getLink(), ""));
    resource.setType(resourceTypeService.get(E_LOCATOR));
    resource.setDoc(getDoc(electronicLocator));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ElectronicLocatorField dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LINK, dto.getLink());
    map.put(NOTE, dto.getNote());
    return coreMapper.toJson(map);
  }
}
