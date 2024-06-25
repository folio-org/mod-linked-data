package org.folio.linked.data.mapper.dto.monograph.common.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.ParallelTitleFieldResponse;
import org.folio.linked.data.domain.dto.ParallelTitleResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PARALLEL_TITLE, predicate = TITLE, requestDto = ParallelTitleField.class)
public class ParallelTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var parallelTitle = coreMapper.toDtoWithEdges(source, ParallelTitleResponse.class, false);
    parallelTitle.setId(String.valueOf(source.getId()));
    if (parentDto instanceof InstanceResponse instance) {
      instance.addTitleItem(new ParallelTitleFieldResponse().parallelTitle(parallelTitle));
    } else if (parentDto instanceof WorkResponse work) {
      work.addTitleItem(new ParallelTitleFieldResponse().parallelTitle(parallelTitle));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var parallelTitle = ((ParallelTitleField) dto).getParallelTitle();
    var resource = new Resource();
    resource.setLabel(getFirstValue(parallelTitle::getMainTitle));
    resource.addTypes(PARALLEL_TITLE);
    resource.setDoc(getDoc(parallelTitle));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ParallelTitle dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PART_NAME, dto.getPartName());
    putProperty(map, PART_NUMBER, dto.getPartNumber());
    putProperty(map, MAIN_TITLE, dto.getMainTitle());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, SUBTITLE, dto.getSubTitle());
    putProperty(map, NOTE, dto.getNote());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
