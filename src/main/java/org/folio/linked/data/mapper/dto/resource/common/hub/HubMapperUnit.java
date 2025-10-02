package org.folio.linked.data.mapper.dto.resource.common.hub;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.HubField;
import org.folio.linked.data.domain.dto.HubRequest;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.TopResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, requestDto = WorkField.class)
public class HubMapperUnit extends TopResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var workDto = ((HubField) dto).getHub();
    var work = new Resource().addTypes(HUB);
    work.setDoc(getDoc(workDto));
    work.setLabel(getFirstValue(() -> getPrimaryMainTitles(workDto.getTitle())));
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getTitle(), TITLE);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getCreatorReference(), CREATOR);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getLanguages(), PredicateDictionary.LANGUAGE);

    work.setId(hashService.hash(work));
    return work;
  }

  private JsonNode getDoc(HubRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, List.of(getFirstValue(() -> getPrimaryMainTitles(dto.getTitle()))));
    return coreMapper.toJson(map);
  }
}
