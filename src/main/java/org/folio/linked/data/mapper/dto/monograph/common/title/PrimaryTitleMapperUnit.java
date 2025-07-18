package org.folio.linked.data.mapper.dto.monograph.common.title;

import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.PrimaryTitle;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.PrimaryTitleFieldResponse;
import org.folio.linked.data.domain.dto.PrimaryTitleResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = TITLE, predicate = PredicateDictionary.TITLE, requestDto = PrimaryTitleField.class)
public class PrimaryTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var primaryTitle = coreMapper.toDtoWithEdges(resourceToConvert, PrimaryTitleResponse.class, false);
    primaryTitle.setId(String.valueOf(resourceToConvert.getId()));
    if (parentDto instanceof InstanceResponse instance) {
      instance.addTitleItem(new PrimaryTitleFieldResponse().primaryTitle(primaryTitle));
    } else if (parentDto instanceof WorkResponse work) {
      work.addTitleItem(new PrimaryTitleFieldResponse().primaryTitle(primaryTitle));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var primaryTitle = ((PrimaryTitleField) dto).getPrimaryTitle();
    var resource = new Resource();
    resource.setLabel(getLabel(getFirstValue(primaryTitle::getMainTitle), getFirstValue(primaryTitle::getSubTitle)));
    resource.addTypes(TITLE);
    resource.setDoc(getDoc(primaryTitle));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(PrimaryTitle dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PART_NAME, dto.getPartName());
    putProperty(map, PART_NUMBER, dto.getPartNumber());
    putProperty(map, MAIN_TITLE, dto.getMainTitle());
    putProperty(map, NON_SORT_NUM, dto.getNonSortNum());
    putProperty(map, SUBTITLE, dto.getSubTitle());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
