package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.Property.DATE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.CopyrightEvent;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = COPYRIGHT_EVENT, predicate = COPYRIGHT, dtoClass = CopyrightEvent.class)
public class CopyrightEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var copyrightEvent = coreMapper.readResourceDoc(source, CopyrightEvent.class);
    copyrightEvent.setId(String.valueOf(source.getResourceHash()));
    return destination.addCopyrightItem(copyrightEvent);
  }

  @Override
  public Resource toEntity(Object dto) {
    var copyrightEvent = (CopyrightEvent) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(copyrightEvent::getDate));
    resource.addType(COPYRIGHT_EVENT);
    resource.setDoc(toDoc(copyrightEvent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode toDoc(CopyrightEvent copyrightEvent) {
    var map = new HashMap<String, List<String>>();
    map.put(DATE, copyrightEvent.getDate());
    return coreMapper.toJson(map);
  }
}
