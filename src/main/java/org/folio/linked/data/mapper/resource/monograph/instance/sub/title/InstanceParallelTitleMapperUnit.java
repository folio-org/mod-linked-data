package org.folio.linked.data.mapper.resource.monograph.instance.sub.title;

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
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PARALLEL_TITLE, predicate = TITLE, dtoClass = ParallelTitleField.class)
public class InstanceParallelTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var parallelTitle = coreMapper.readResourceDoc(source, ParallelTitle.class);
    parallelTitle.setId(String.valueOf(source.getResourceHash()));
    destination.addTitleItem(new ParallelTitleField().parallelTitle(parallelTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var parallelTitle = ((ParallelTitleField) dto).getParallelTitle();
    var resource = new Resource();
    resource.setLabel(getFirstValue(parallelTitle::getMainTitle));
    resource.addType(PARALLEL_TITLE);
    resource.setDoc(getDoc(parallelTitle));
    resource.setResourceHash(coreMapper.hash(resource));
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