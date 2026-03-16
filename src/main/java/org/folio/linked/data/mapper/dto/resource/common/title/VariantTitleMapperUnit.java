package org.folio.linked.data.mapper.dto.resource.common.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.domain.dto.VariantTitleFieldResponse;
import org.folio.linked.data.domain.dto.VariantTitleResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.dto.HasTitle;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@MapperUnit(type = VARIANT_TITLE, predicate = TITLE, requestDto = VariantTitleField.class)
public class VariantTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;

  public VariantTitleMapperUnit(CoreMapper coreMapper, HashService hashService,
                                ResourceEntityLabelService labelService) {
    super(hashService, labelService);
    this.coreMapper = coreMapper;
  }

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
  protected ResourceTypeDictionary resourceType() {
    return VARIANT_TITLE;
  }

  @Override
  protected JsonNode getDoc(Object dto) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var map = getBaseTitleProperties(variantTitle);
    putProperty(map, DATE, variantTitle.getDate());
    putProperty(map, VARIANT_TYPE, variantTitle.getVariantType());
    putProperty(map, NOTE, variantTitle.getNote());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
