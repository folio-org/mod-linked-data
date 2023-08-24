package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
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
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.VariantTitle2;
import org.folio.linked.data.domain.dto.VariantTitleField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = VariantTitleField2.class)
public class InstanceVariantTitle2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final NoteMapperUnit<VariantTitle2> noteMapper;
  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var variantTitle = coreMapper.readResourceDoc(source, VariantTitle2.class);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, variantTitle);
    destination.addTitleItem(new VariantTitleField2().variantTitle(variantTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var variantTitle = ((VariantTitleField2) dto).getVariantTitle();
    var resource = new Resource();
    resource.setLabel(VARIANT_TITLE_URL);
    resource.setType(resourceTypeService.get(VARIANT_TITLE));
    resource.setDoc(getDoc(variantTitle));
    coreMapper.mapResourceEdges(variantTitle.getNote(), resource, NOTE, NOTE_PRED,
      (fieldDto, pred) -> noteMapper.toEntity(fieldDto, pred, null));
    coreMapper.mapResourceEdges(variantTitle.getNote(), resource, null, NOTE_PRED,
      (fieldDto, pred) -> noteMapper.toEntity(fieldDto, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(VariantTitle2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(DATE_URL, dto.getDate());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    map.put(VARIANT_TYPE_URL, dto.getVariantType());
    return coreMapper.toJson(map);
  }
}
