package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LCCN_URL;
import static org.folio.linked.data.util.Bibframe2Constants.STATUS_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.STATUS_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Lccn2;
import org.folio.linked.data.domain.dto.LccnField2;
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
@MapperUnit(type = IDENTIFIERS_LCCN_URL, predicate = IDENTIFIED_BY_PRED, dtoClass = LccnField2.class)
public class Lccn2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var lccn = coreMapper.readResourceDoc(source, Lccn2.class);
    coreMapper.addMappedProperties(source, STATUS_PRED, lccn::addStatusItem);
    destination.addIdentifiedByItem(new LccnField2().lccn(lccn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var lccn = ((LccnField2) dto).getLccn();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_LCCN_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_LCCN));
    resource.setDoc(getDoc(lccn));
    coreMapper.mapPropertyEdges(lccn.getStatus(), resource, STATUS_PRED, STATUS_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Lccn2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_PRED, dto.getValue());
    return coreMapper.toJson(map);
  }
}
