package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TYPE;
import static org.folio.linked.data.util.BibframeUtils.getLabelOrFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = VariantTitleField.class)
public class InstanceVariantTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var variantTitle = coreMapper.readResourceDoc(source, VariantTitle.class);
    variantTitle.setId(source.getResourceHash());
    variantTitle.setLabel(source.getLabel());
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(getLabelOrFirstValue(variantTitle.getLabel(), variantTitle::getMainTitle));
    resource.addType(resourceTypeService.get(VARIANT_TITLE));
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
    map.put(SUBTITLE, dto.getSubtitle());
    map.put(VARIANT_TYPE, dto.getVariantType());
    map.put(NOTE, dto.getNote());
    return coreMapper.toJson(map);
  }
}
