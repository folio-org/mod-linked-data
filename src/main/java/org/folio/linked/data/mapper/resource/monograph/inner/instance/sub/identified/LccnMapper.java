package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN_URL;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.STATUS_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = IDENTIFIERS_LCCN, predicate = IDENTIFIED_BY_PRED, dtoClass = LccnField.class)
public class LccnMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var lccn = commonMapper.readResourceDoc(source, Lccn.class);
    commonMapper.addMappedProperties(source, STATUS_PRED, lccn::addStatusItem);
    destination.addIdentifiedByItem(new LccnField().lccn(lccn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var lccn = ((LccnField) dto).getLccn();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_LCCN_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_LCCN));
    resource.setDoc(getDoc(lccn));
    commonMapper.mapPropertyEdges(lccn.getStatus(), resource, STATUS_PRED, STATUS_URL);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Lccn dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_URL, dto.getValue());
    return commonMapper.toJson(map);
  }
}
