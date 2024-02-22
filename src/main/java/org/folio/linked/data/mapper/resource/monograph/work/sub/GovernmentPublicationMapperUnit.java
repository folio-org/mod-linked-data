package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GovernmentPublication;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CATEGORY, predicate = GOVERNMENT_PUBLICATION, dtoClass = GovernmentPublication.class)
public class GovernmentPublicationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var governmentPublication = coreMapper.toDtoWithEdges(source, GovernmentPublication.class, false);
    governmentPublication.setId(String.valueOf(source.getResourceHash()));
    governmentPublication.setLabel(source.getLabel());
    if (parentDto instanceof Work work) {
      work.addGovernmentPublicationItem(governmentPublication);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var governmentPublication = (GovernmentPublication) dto;
    var resource = new Resource();
    resource.setLabel(governmentPublication.getLabel());
    resource.addType(CATEGORY);
    resource.setDoc(getDoc(governmentPublication));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(GovernmentPublication dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, LINK, dto.getLink());
    putProperty(map, TERM, dto.getTerm());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
