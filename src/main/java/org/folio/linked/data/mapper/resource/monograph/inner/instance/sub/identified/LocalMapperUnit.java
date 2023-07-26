package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_URL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierLocal;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.LocalIdentifierField;
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
@MapperUnit(type = IDENTIFIERS_LOCAL, predicate = IDENTIFIED_BY_PRED, dtoClass = LocalIdentifierField.class)
public class LocalMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var local = coreMapper.readResourceDoc(source, IdentifierLocal.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, local::addAssignerItem);
    destination.addIdentifiedByItem(new LocalIdentifierField().local(local));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var local = ((LocalIdentifierField) dto).getLocal();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_LOCAL_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_LOCAL));
    resource.setDoc(getDoc(local));
    coreMapper.mapPropertyEdges(local.getAssigner(), resource, ASSIGNER_PRED, ASSIGNER_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(IdentifierLocal dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_PRED, dto.getValue());
    return coreMapper.toJson(map);
  }
}
