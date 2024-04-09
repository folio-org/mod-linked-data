package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_LCCN, predicate = MAP, dtoClass = LccnField.class)
public class LccnMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof Instance instance) {
      var lccn = coreMapper.toDtoWithEdges(source, Lccn.class, false);
      lccn.setId(String.valueOf(source.getId()));
      instance.addMapItem(new LccnField().lccn(lccn));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var lccn = ((LccnField) dto).getLccn();
    var resource = new Resource();
    resource.setLabel(getFirstValue(lccn::getValue));
    resource.addTypes(IDENTIFIER, ID_LCCN);
    resource.setDoc(getDoc(lccn));
    coreMapper.addOutgoingEdges(resource, Lccn.class, lccn.getStatus(), STATUS);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Lccn dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getValue());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
