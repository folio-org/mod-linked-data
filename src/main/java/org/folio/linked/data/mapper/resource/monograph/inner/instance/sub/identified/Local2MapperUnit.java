package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LOCAL_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierLocal2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.LocalIdentifierField2;
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
@MapperUnit(type = IDENTIFIERS_LOCAL_URL, predicate = IDENTIFIED_BY_PRED, dtoClass = LocalIdentifierField2.class)
public class Local2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var local = coreMapper.readResourceDoc(source, IdentifierLocal2.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, local::addAssignerItem);
    destination.addIdentifiedByItem(new LocalIdentifierField2().local(local));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var local = ((LocalIdentifierField2) dto).getLocal();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_LOCAL_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_LOCAL));
    resource.setDoc(getDoc(local));
    coreMapper.mapPropertyEdges(local.getAssigner(), resource, ASSIGNER_PRED, ASSIGNER_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(IdentifierLocal2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_PRED, dto.getValue());
    return coreMapper.toJson(map);
  }
}
