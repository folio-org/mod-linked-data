package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.ITEM_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.linked.data.util.ResourceUtils.putProperty;
import static org.folio.marc4ld.util.Constants.Classification.DDC;
import static org.folio.marc4ld.util.Constants.Classification.LC;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Classification;
import org.folio.linked.data.domain.dto.ClassificationResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ResourceTypeDictionary.CLASSIFICATION, predicate = CLASSIFICATION, requestDto = Classification.class)
public class ClassificationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var classification = coreMapper.toDtoWithEdges(resourceToConvert, ClassificationResponse.class, false);
    classification.setId(String.valueOf(resourceToConvert.getId()));
    if (parentDto instanceof WorkResponse work) {
      work.addClassificationItem(classification);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var classification = (Classification) dto;
    var resource = new Resource();
    resource.addTypes(ResourceTypeDictionary.CLASSIFICATION);
    resource.setDoc(getDoc(classification));
    coreMapper.addOutgoingEdges(resource, Classification.class,
      classification.getAssigningSourceReference(), ASSIGNING_SOURCE);
    if (LC.equals(classification.getSource().getFirst())) {
      coreMapper.addOutgoingEdges(resource, Classification.class,
        classification.getStatus(), STATUS);
    }
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Classification dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, SOURCE, dto.getSource());
    putProperty(map, ITEM_NUMBER, dto.getItemNumber());
    if (DDC.equals(dto.getSource().getFirst())) {
      putProperty(map, EDITION_NUMBER, dto.getEditionNumber());
      putProperty(map, EDITION, dto.getEdition());
    }
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
