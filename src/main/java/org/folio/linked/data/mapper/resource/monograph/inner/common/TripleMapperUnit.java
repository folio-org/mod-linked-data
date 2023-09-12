package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.TERM;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Triple;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.dictionary.ResourceTypeService;

@RequiredArgsConstructor
public abstract class TripleMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final ResourceTypeService resourceTypeService;
  private final BiFunction<Triple, Instance, Instance> tripleConsumer;
  private final String type;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var triple = coreMapper.readResourceDoc(source, Triple.class);
    return tripleConsumer.apply(triple, destination);
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var triple = (Triple) dto;
    var resource = new Resource();
    resource.setLabel(getFirst(triple.getTerm(), ""));
    resource.addType(resourceTypeService.get(type));
    resource.setDoc(getDoc(triple));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Triple dto) {
    var map = new HashMap<String, List<String>>();
    map.put(CODE, dto.getCode());
    map.put(TERM, dto.getTerm());
    map.put(LINK, dto.getLink());
    return coreMapper.toJson(map);
  }
}
