package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL_URL;
import static org.folio.linked.data.util.BibframeConstants.STATUS_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.hash;
import static org.folio.linked.data.util.MappingUtil.mapPropertyEdges;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;
import static org.folio.linked.data.util.MappingUtil.toJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierLocal;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.LocalIdentifierField;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = IDENTIFIERS_LOCAL, predicate = IDENTIFIED_BY_PRED, dtoClass = LocalIdentifierField.class)
public class LocalMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var local = readResourceDoc(mapper, source, IdentifierLocal.class);
    addMappedProperties(mapper, source, ASSIGNER_PRED, local::addAssignerItem);
    destination.addIdentifiedByItem(new LocalIdentifierField().local(local));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var local = ((LocalIdentifierField) dto).getLocal();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_LOCAL_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_LOCAL));
    resource.setDoc(toJson(getDoc(local), mapper));
    mapPropertyEdges(local.getAssigner(), resource, () -> predicateService.get(ASSIGNER_PRED),
      () -> resourceTypeService.get(STATUS_URL), mapper);
    resource.setResourceHash(hash(resource, mapper));
    return resource;
  }

  private Map<String, List<String>> getDoc(IdentifierLocal dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_URL, dto.getValue());
    return map;
  }
}
