package org.folio.linked.data.mapper.dto.resource.common.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.ParallelTitleFieldResponse;
import org.folio.linked.data.domain.dto.ParallelTitleResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.dto.HasTitle;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@MapperUnit(type = PARALLEL_TITLE, predicate = TITLE, requestDto = ParallelTitleField.class)
public class ParallelTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;

  public ParallelTitleMapperUnit(CoreMapper coreMapper, HashService hashService,
                                 ResourceEntityLabelService labelService) {
    super(hashService, labelService);
    this.coreMapper = coreMapper;
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var parallelTitle = coreMapper.toDtoWithEdges(resourceToConvert, ParallelTitleResponse.class, false);
    parallelTitle.setId(String.valueOf(resourceToConvert.getId()));
    var titleField = new ParallelTitleFieldResponse().parallelTitle(parallelTitle);
    if (parentDto instanceof HasTitle dtoWithTitle) {
      dtoWithTitle.addTitleItem(titleField);
    }
    return parentDto;
  }

  @Override
  protected ResourceTypeDictionary resourceType() {
    return PARALLEL_TITLE;
  }

  @Override
  protected JsonNode getDoc(Object dto) {
    var parallelTitle = ((ParallelTitleField) dto).getParallelTitle();
    var map = getBaseTitleProperties(parallelTitle);
    putProperty(map, DATE, parallelTitle.getDate());
    putProperty(map, NOTE, parallelTitle.getNote());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
