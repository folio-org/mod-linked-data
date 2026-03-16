package org.folio.linked.data.mapper.dto.resource.common.title;

import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.PrimaryTitleFieldResponse;
import org.folio.linked.data.domain.dto.PrimaryTitleResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.dto.HasTitle;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@MapperUnit(type = TITLE, predicate = PredicateDictionary.TITLE, requestDto = PrimaryTitleField.class)
public class PrimaryTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;

  public PrimaryTitleMapperUnit(CoreMapper coreMapper, HashService hashService,
                                ResourceEntityLabelService labelService) {
    super(hashService, labelService);
    this.coreMapper = coreMapper;
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var primaryTitle = coreMapper.toDtoWithEdges(resourceToConvert, PrimaryTitleResponse.class, false);
    primaryTitle.setId(String.valueOf(resourceToConvert.getId()));
    var titleField = new PrimaryTitleFieldResponse().primaryTitle(primaryTitle);
    if (parentDto instanceof HasTitle dtoWithTitle) {
      dtoWithTitle.addTitleItem(titleField);
    }
    return parentDto;
  }

  @Override
  protected ResourceTypeDictionary resourceType() {
    return TITLE;
  }

  @Override
  protected JsonNode getDoc(Object dto) {
    var primaryTitle = ((PrimaryTitleField) dto).getPrimaryTitle();
    var map = getBaseTitleProperties(primaryTitle);
    putProperty(map, NON_SORT_NUM, primaryTitle.getNonSortNum());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
