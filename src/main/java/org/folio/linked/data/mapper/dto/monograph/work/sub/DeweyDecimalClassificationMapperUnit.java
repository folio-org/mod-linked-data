package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.ITEM_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.DeweyDecimalClassification;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ResourceTypeDictionary.CLASSIFICATION, predicate = CLASSIFICATION,
  dtoClass = DeweyDecimalClassification.class)
public class DeweyDecimalClassificationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var deweyDecimalClassification = coreMapper.toDtoWithEdges(source, DeweyDecimalClassification.class, false);
    deweyDecimalClassification.setId(String.valueOf(source.getId()));
    if (parentDto instanceof Work work) {
      work.addClassificationItem(deweyDecimalClassification);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var deweyDecimalClassification = (DeweyDecimalClassification) dto;
    var resource = new Resource();
    resource.addTypes(ResourceTypeDictionary.CLASSIFICATION);
    resource.setDoc(getDoc(deweyDecimalClassification));
    coreMapper.addOutgoingEdges(resource, DeweyDecimalClassification.class,
      deweyDecimalClassification.getAssigningSourceReference(), ASSIGNING_SOURCE);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(DeweyDecimalClassification dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, SOURCE, dto.getSource());
    putProperty(map, ITEM_NUMBER, dto.getItemNumber());
    putProperty(map, EDITION_NUMBER, dto.getEditionNumber());
    putProperty(map, EDITION, dto.getEdition());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
