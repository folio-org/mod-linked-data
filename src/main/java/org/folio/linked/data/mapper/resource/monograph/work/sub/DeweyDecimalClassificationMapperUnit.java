package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.DeweyDecimalClassification;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CATEGORY, predicate = CLASSIFICATION, dtoClass = DeweyDecimalClassification.class)
public class DeweyDecimalClassificationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public <T> T toDto(Resource source, T destination) {
    var deweyDecimalClassification = coreMapper.readResourceDoc(source, DeweyDecimalClassification.class);
    deweyDecimalClassification.setId(String.valueOf(source.getResourceHash()));
    if (destination instanceof Work work) {
      work.addClassificationItem(deweyDecimalClassification);
    }
    if (destination instanceof WorkReference work) {
      work.addClassificationItem(deweyDecimalClassification);
    }
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var deweyDecimalClassification = (DeweyDecimalClassification) dto;
    var resource = new Resource();
    resource.addType(CATEGORY);
    resource.setDoc(getDoc(deweyDecimalClassification));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(DeweyDecimalClassification dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, SOURCE, dto.getSource());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
