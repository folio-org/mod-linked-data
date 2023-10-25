package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.CATEGORY;
import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.SOURCE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.DewvyDecimalClassification;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CATEGORY, predicate = CLASSIFICATION_PRED, dtoClass = DewvyDecimalClassification.class)
public class DeweyDecimalClassificationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;


  @Override
  public Work toDto(Resource source, Work destination) {
    var deweyDecimalClassification = coreMapper.readResourceDoc(source, DewvyDecimalClassification.class);
    deweyDecimalClassification.setId(String.valueOf(source.getResourceHash()));
    destination.addClassificationItem(deweyDecimalClassification);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var dewvy = (DewvyDecimalClassification) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(dewvy::getCode));
    resource.addType(resourceTypeService.get(CATEGORY));
    resource.setDoc(getDoc(dewvy));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(DewvyDecimalClassification dto) {
    var map = new HashMap<String, List<String>>();
    map.put(CODE, dto.getCode());
    map.put(SOURCE, dto.getSource());
    return coreMapper.toJson(map);
  }
}
