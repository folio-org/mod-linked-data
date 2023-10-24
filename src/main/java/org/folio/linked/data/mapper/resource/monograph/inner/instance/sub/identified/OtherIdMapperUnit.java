package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.OtherId;
import org.folio.linked.data.domain.dto.OtherIdField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_UNKNOWN, predicate = MAP, dtoClass = OtherIdField.class)
public class OtherIdMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var otherId = coreMapper.readResourceDoc(source, OtherId.class);
    otherId.setId(String.valueOf(source.getResourceHash()));
    destination.addMapItem(new OtherIdField().identifier(otherId));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var otherId = ((OtherIdField) dto).getIdentifier();
    var resource = new Resource();
    resource.setLabel(getFirstValue(otherId::getValue));
    resource.addType(ID_UNKNOWN);
    resource.setDoc(getDoc(otherId));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(OtherId dto) {
    var map = new HashMap<String, List<String>>();
    map.put(NAME.getValue(), dto.getValue());
    map.put(QUALIFIER.getValue(), dto.getQualifier());
    return coreMapper.toJson(map);
  }
}
