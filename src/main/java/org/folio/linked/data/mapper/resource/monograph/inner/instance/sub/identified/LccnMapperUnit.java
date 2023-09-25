package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.STATUS;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeUtils.getLabelOrFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.StatusMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = LCCN, predicate = MAP_PRED, dtoClass = LccnField.class)
public class LccnMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final StatusMapperUnit<Lccn> statusMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var lccn = coreMapper.readResourceDoc(source, Lccn.class);
    lccn.setId(source.getResourceHash());
    lccn.setLabel(source.getLabel());
    coreMapper.addMappedResources(statusMapper, source, STATUS_PRED, lccn);
    destination.addMapItem(new LccnField().lccn(lccn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var lccn = ((LccnField) dto).getLccn();
    var resource = new Resource();
    resource.setLabel(getLabelOrFirstValue(lccn.getLabel(), lccn::getValue));
    resource.addType(resourceTypeService.get(LCCN));
    resource.setDoc(getDoc(lccn));
    coreMapper.mapResourceEdges(lccn.getStatus(), resource, STATUS, STATUS_PRED,
      (status, pred) -> statusMapper.toEntity(status, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Lccn dto) {
    var map = new HashMap<String, List<String>>();
    map.put(NAME, dto.getValue());
    return coreMapper.toJson(map);
  }
}
