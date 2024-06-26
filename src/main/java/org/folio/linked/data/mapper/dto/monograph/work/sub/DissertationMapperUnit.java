package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GRANTING_INSTITUTION;
import static org.folio.ld.dictionary.PropertyDictionary.DEGREE;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_ID;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_YEAR;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ResourceTypeDictionary.DISSERTATION, predicate = DISSERTATION, dtoClass = Dissertation.class)
public class DissertationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var dissertation = coreMapper.toDtoWithEdges(source, Dissertation.class, false);
    dissertation.setId(String.valueOf(source.getId()));
    if (parentDto instanceof Work work) {
      work.addDissertationItem(dissertation);
    } else if (parentDto  instanceof WorkReference workReference) {
      workReference.addDissertationItem(dissertation);
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
      dissertation.getGrantingInstitutionReference(), GRANTING_INSTITUTION);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Dissertation dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, dto.getLabel());
    putProperty(map, DEGREE, dto.getDegree());
    putProperty(map, DISSERTATION_YEAR, dto.getDissertationYear());
    putProperty(map, DISSERTATION_NOTE, dto.getDissertationNote());
    putProperty(map, DISSERTATION_ID, dto.getDissertationID());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
