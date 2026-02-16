package org.folio.linked.data.mapper.dto.resource.common.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.domain.dto.VariantTitleFieldResponse;
import org.folio.linked.data.domain.dto.VariantTitleResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.dto.HasTitle;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE, predicate = TITLE, requestDto = VariantTitleField.class)
public class VariantTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var variantTitle = coreMapper.toDtoWithEdges(resourceToConvert, VariantTitleResponse.class, false);
    variantTitle.setId(String.valueOf(resourceToConvert.getId()));
    var titleField = new VariantTitleFieldResponse().variantTitle(variantTitle);
    if (parentDto instanceof HasTitle dtoWithTitle) {
      dtoWithTitle.addTitleItem(titleField);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(getLabel(getFirstValue(variantTitle::getMainTitle), getFirstValue(variantTitle::getSubTitle)));
    resource.addTypes(VARIANT_TITLE);
    resource.setDoc(getDoc(variantTitle));
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(VariantTitle dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PART_NAME, dto.getPartName());
    putProperty(map, PART_NUMBER, dto.getPartNumber());
    putProperty(map, MAIN_TITLE, dto.getMainTitle());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, SUBTITLE, dto.getSubTitle());
    putProperty(map, VARIANT_TYPE, dto.getVariantType());
    putProperty(map, NOTE, dto.getNote());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
