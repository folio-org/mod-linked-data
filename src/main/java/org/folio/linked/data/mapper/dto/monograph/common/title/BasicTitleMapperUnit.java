package org.folio.linked.data.mapper.dto.monograph.common.title;

import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.BasicTitle;
import org.folio.linked.data.domain.dto.BasicTitleField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceReference;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = TITLE, predicate = PredicateDictionary.TITLE, dtoClass = BasicTitleField.class)
public class BasicTitleMapperUnit extends TitleMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var basicTitle = coreMapper.toDtoWithEdges(source, BasicTitle.class, false);
    basicTitle.setId(String.valueOf(source.getId()));
    if (parentDto instanceof Instance instance) {
      instance.addTitleItem(new BasicTitleField().basicTitle(basicTitle));
    } else if (parentDto instanceof InstanceReference instanceReference) {
      instanceReference.addTitleItem(new BasicTitleField().basicTitle(basicTitle));
    } else if (parentDto instanceof Work work) {
      work.addTitleItem(new BasicTitleField().basicTitle(basicTitle));
    } else if (parentDto instanceof WorkReference workReference) {
      workReference.addTitleItem(new BasicTitleField().basicTitle(basicTitle));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var basicTitle = ((BasicTitleField) dto).getBasicTitle();
    var resource = new Resource();
    resource.setLabel(getFirstValue(basicTitle::getMainTitle));
    resource.addType(TITLE);
    resource.setDoc(getDoc(basicTitle));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(BasicTitle dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PART_NAME, dto.getPartName());
    putProperty(map, PART_NUMBER, dto.getPartNumber());
    putProperty(map, MAIN_TITLE, dto.getMainTitle());
    putProperty(map, NON_SORT_NUM, dto.getNonSortNum());
    putProperty(map, SUBTITLE, dto.getSubTitle());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
