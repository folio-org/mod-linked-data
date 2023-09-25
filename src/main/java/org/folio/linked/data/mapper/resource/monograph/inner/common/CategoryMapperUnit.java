package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.TERM;
import static org.folio.linked.data.util.BibframeUtils.getLabelOrFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.dictionary.ResourceTypeService;

@RequiredArgsConstructor
public abstract class CategoryMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final ResourceTypeService resourceTypeService;
  private final BiFunction<Category, Instance, Instance> categoryConsumer;
  private final String type;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var category = coreMapper.readResourceDoc(source, Category.class);
    category.setId(source.getResourceHash());
    category.setLabel(source.getLabel());
    return categoryConsumer.apply(category, destination);
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var category = (Category) dto;
    var resource = new Resource();
    resource.setLabel(getLabelOrFirstValue(category.getLabel(), category::getTerm));
    resource.addType(resourceTypeService.get(type));
    resource.setDoc(getDoc(category));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Category dto) {
    var map = new HashMap<String, List<String>>();
    map.put(CODE, dto.getCode());
    map.put(TERM, dto.getTerm());
    map.put(LINK, dto.getLink());
    return coreMapper.toJson(map);
  }
}
