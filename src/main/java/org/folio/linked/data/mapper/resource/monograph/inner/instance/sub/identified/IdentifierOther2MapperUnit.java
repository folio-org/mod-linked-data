package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_OTHER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.QUALIFIER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierOther2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.OtherIdentifierField2;
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
@MapperUnit(type = IDENTIFIERS_OTHER_URL, predicate = IDENTIFIED_BY_PRED, dtoClass = OtherIdentifierField2.class)
public class IdentifierOther2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var identifier = coreMapper.readResourceDoc(source, IdentifierOther2.class);
    destination.addIdentifiedByItem(new OtherIdentifierField2().identifier(identifier));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var other = ((OtherIdentifierField2) dto).getIdentifier();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_OTHER_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_OTHER));
    resource.setDoc(getDoc(other));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(IdentifierOther2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_PRED, dto.getValue());
    map.put(QUALIFIER_URL, dto.getQualifier());
    return coreMapper.toJson(map);
  }

}
