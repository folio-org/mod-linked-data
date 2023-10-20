package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.Property.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.StatusMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_LCCN, predicate = MAP, dtoClass = LccnField.class)
public class LccnMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final StatusMapperUnit<Lccn> statusMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var lccn = coreMapper.readResourceDoc(source, Lccn.class);
    lccn.setId(String.valueOf(source.getResourceHash()));
    coreMapper.addMappedResources(statusMapper, source, STATUS, lccn);
    destination.addMapItem(new LccnField().lccn(lccn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var lccn = ((LccnField) dto).getLccn();
    var resource = new Resource();
    resource.setLabel(getFirstValue(lccn::getValue));
    resource.addType(ID_LCCN);
    resource.setDoc(getDoc(lccn));
    coreMapper.mapSubEdges(lccn.getStatus(), resource, STATUS, statusMapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Lccn dto) {
    var map = new HashMap<String, List<String>>();
    map.put(NAME, dto.getValue());
    return coreMapper.toJson(map);
  }
}
