package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.Property.DATE;
import static org.folio.ld.dictionary.Property.MAIN_TITLE;
import static org.folio.ld.dictionary.Property.NOTE;
import static org.folio.ld.dictionary.Property.PART_NAME;
import static org.folio.ld.dictionary.Property.PART_NUMBER;
import static org.folio.ld.dictionary.Property.SUBTITLE;
import static org.folio.ld.dictionary.Property.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE, predicate = TITLE, dtoClass = VariantTitleField.class)
public class InstanceVariantTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var variantTitle = coreMapper.readResourceDoc(source, VariantTitle.class);
    variantTitle.setId(String.valueOf(source.getResourceHash()));
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(getFirstValue(variantTitle::getMainTitle));
    resource.addType(VARIANT_TITLE);
    resource.setDoc(getDoc(variantTitle));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(VariantTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME, dto.getPartName());
    map.put(PART_NUMBER, dto.getPartNumber());
    map.put(MAIN_TITLE, dto.getMainTitle());
    map.put(DATE, dto.getDate());
    map.put(SUBTITLE, dto.getSubTitle());
    map.put(VARIANT_TYPE, dto.getVariantType());
    map.put(NOTE, dto.getNote());
    return coreMapper.toJson(map);
  }
}
