package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.Property.DATE;
import static org.folio.ld.dictionary.Property.MAIN_TITLE;
import static org.folio.ld.dictionary.Property.NOTE;
import static org.folio.ld.dictionary.Property.PART_NAME;
import static org.folio.ld.dictionary.Property.PART_NUMBER;
import static org.folio.ld.dictionary.Property.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
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
    map.put(PART_NAME, dto.getPartName());
    map.put(PART_NUMBER, dto.getPartNumber());
    map.put(MAIN_TITLE, dto.getMainTitle());
    map.put(DATE, dto.getDate());
    map.put(SUBTITLE, dto.getSubTitle());
    map.put(NOTE, dto.getNote());
    return coreMapper.toJson(map);
  }

}
