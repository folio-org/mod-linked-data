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
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.hash;
import static org.folio.linked.data.util.MappingUtil.mapResourceEdges;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;
import static org.folio.linked.data.util.MappingUtil.toJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceNoteMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = VARIANT_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = VariantTitleField.class)
public class InstanceVariantTitleMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final InstanceNoteMapper noteMapper;
  private final ObjectMapper mapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var variantTitle = readResourceDoc(mapper, source, VariantTitle.class);
    addMappedProperties(mapper, source, NOTE_PRED, variantTitle::addNoteItem);
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var variantTitle = ((VariantTitleField) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(VARIANT_TITLE_URL);
    resource.setType(resourceTypeService.get(VARIANT_TITLE));
    resource.setDoc(toJson(getDoc(variantTitle), mapper));
    mapResourceEdges(variantTitle.getNote(), resource, () -> predicateService.get(NOTE_PRED), noteMapper::toEntity);
    resource.setResourceHash(hash(resource, mapper));
    return resource;
  }

  private Map<String, List<String>> getDoc(VariantTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(DATE_URL, dto.getDate());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    map.put(VARIANT_TYPE_URL, dto.getVariantType());
    return map;
  }
}
