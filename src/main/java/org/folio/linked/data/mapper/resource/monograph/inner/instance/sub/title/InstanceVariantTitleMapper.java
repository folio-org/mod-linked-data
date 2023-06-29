package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME_URL;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER_URL;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TYPE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceNoteMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = VARIANT_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = VariantTitleField.class)
public class InstanceVariantTitleMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final InstanceNoteMapper noteMapper;
  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var variantTitle = commonMapper.readResourceDoc(source, VariantTitle.class);
    commonMapper.addMappedProperties(source, NOTE_PRED, variantTitle::addNoteItem);
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(VARIANT_TITLE_URL);
    resource.setType(resourceTypeService.get(VARIANT_TITLE));
    resource.setDoc(getDoc(variantTitle));
    commonMapper.mapResourceEdges(variantTitle.getNote(), resource, NOTE_PRED, noteMapper::toEntity);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(VariantTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(DATE_URL, dto.getDate());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    map.put(VARIANT_TYPE_URL, dto.getVariantType());
    return commonMapper.toJson(map);
  }
}
