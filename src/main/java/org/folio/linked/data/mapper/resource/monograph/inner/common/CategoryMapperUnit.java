package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.ld.dictionary.Property.CODE;
import static org.folio.ld.dictionary.Property.LINK;
import static org.folio.ld.dictionary.Property.TERM;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

@RequiredArgsConstructor
public abstract class CategoryMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final BiFunction<Category, Instance, Instance> categoryConsumer;
  private final ResourceTypeDictionary type;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var category = coreMapper.readResourceDoc(source, Category.class);
    category.setId(String.valueOf(source.getResourceHash()));
    return categoryConsumer.apply(category, destination);
  }

  @Override
  public Resource toEntity(Object dto) {
    var category = (Category) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(category::getTerm));
    resource.addType(type);
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
