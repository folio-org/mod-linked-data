package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.CopyrightEvent;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = COPYRIGHT_EVENT, predicate = COPYRIGHT, requestDto = CopyrightEvent.class)
public class CopyrightEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof InstanceResponse instance) {
      var copyrightEvent = coreMapper.toDtoWithEdges(source, CopyrightEvent.class, false);
      copyrightEvent.setId(String.valueOf(source.getId()));
      instance.addCopyrightItem(copyrightEvent);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var copyrightEvent = (CopyrightEvent) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(copyrightEvent::getDate));
    resource.addTypes(COPYRIGHT_EVENT);
    resource.setDoc(getDoc(copyrightEvent));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(CopyrightEvent dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, DATE, dto.getDate());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
