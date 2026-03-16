package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.DEGREE_GRANTING_INSTITUTION;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DEGREE;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_ID;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ResourceTypeDictionary.DISSERTATION, predicate = DISSERTATION, requestDto = Dissertation.class)
public class DissertationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;
  private final ResourceEntityLabelService labelService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var dissertation = coreMapper.toDtoWithEdges(resourceToConvert, DissertationResponse.class, false);
    dissertation.setId(String.valueOf(resourceToConvert.getId()));
    if (parentDto instanceof WorkResponse work) {
      work.addDissertationItem(dissertation);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var dissertation = (Dissertation) dto;
    var resource = new Resource();
    resource.addTypes(ResourceTypeDictionary.DISSERTATION);
    resource.setDoc(getDoc(dissertation));
    coreMapper.addOutgoingEdges(resource, Dissertation.class,
      dissertation.getGrantingInstitutionReference(), DEGREE_GRANTING_INSTITUTION);
    labelService.assignLabelToResource(resource);
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Dissertation dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NOTE, dto.getNote());
    putProperty(map, DEGREE, dto.getDegree());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    putProperty(map, DISSERTATION_ID, dto.getDissertationID());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
