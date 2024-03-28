package org.folio.linked.data.mapper.dto.monograph.instance.sub.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceReference;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE, predicate = TITLE, dtoClass = VariantTitleField.class)
public class InstanceVariantTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var variantTitle = coreMapper.toDtoWithEdges(source, VariantTitle.class, false);
    variantTitle.setId(String.valueOf(source.getId()));
    if (parentDto instanceof Instance instance) {
      instance.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    }
    if (parentDto instanceof InstanceReference instance) {
      instance.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(getFirstValue(variantTitle::getMainTitle));
    resource.addType(VARIANT_TITLE);
    resource.setDoc(getDoc(variantTitle));
    resource.setId(hashService.hash(resource));
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
