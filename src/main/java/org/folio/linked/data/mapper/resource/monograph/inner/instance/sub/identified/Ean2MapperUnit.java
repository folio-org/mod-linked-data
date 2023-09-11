package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_EAN_URL;
import static org.folio.linked.data.util.Bibframe2Constants.QUALIFIER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Ean2;
import org.folio.linked.data.domain.dto.EanField2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = IDENTIFIERS_EAN_URL, predicate = IDENTIFIED_BY_PRED, dtoClass = EanField2.class)
public class Ean2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var ean = coreMapper.readResourceDoc(source, Ean2.class);
    destination.addIdentifiedByItem(new EanField2().ean(ean));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var ean = ((EanField2) dto).getEan();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_EAN_URL);
    resource.addType(resourceTypeService.get(IDENTIFIERS_EAN));
    resource.setDoc(getDoc(ean));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Ean2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_PRED, dto.getValue());
    map.put(QUALIFIER_URL, dto.getQualifier());
    return coreMapper.toJson(map);
  }

}
