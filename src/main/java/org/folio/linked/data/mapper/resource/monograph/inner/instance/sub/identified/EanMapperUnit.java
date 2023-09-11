package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Ean;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = EAN, predicate = MAP_PRED, dtoClass = EanField.class)
public class EanMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var ean = coreMapper.readResourceDoc(source, Ean.class);
    destination.addMapItem(new EanField().ean(ean));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var ean = ((EanField) dto).getEan();
    var resource = new Resource();
    resource.setLabel(getFirst(ean.getValue(), ""));
    resource.addType(resourceTypeService.get(EAN));
    resource.setDoc(getDoc(ean));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Ean dto) {
    var map = new HashMap<String, List<String>>();
    map.put(EAN_VALUE, dto.getValue());
    map.put(QUALIFIER, dto.getQualifier());
    return coreMapper.toJson(map);
  }
}
