package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.Bibframe2Constants.DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NAME_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NUMBER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SUBTITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TYPE_URL;

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
import org.folio.linked.data.mapper.resource.monograph.inner.common.Note2MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE_URL, predicate = INSTANCE_TITLE_2_PRED, dtoClass = VariantTitleField2.class)
public class InstanceVariantTitle2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final Note2MapperUnit<VariantTitle2> noteMapper;
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
    resource.addType(resourceTypeService.get(VARIANT_TITLE_2));
    resource.setDoc(getDoc(variantTitle));
    coreMapper.mapResourceEdges(variantTitle.getNote(), resource, NOTE_URL, NOTE_PRED,
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
