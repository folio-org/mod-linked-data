package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CONTENT, dtoClass = Category.class)
@RequiredArgsConstructor
public class ContentMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public <T> T toDto(Resource source, T parentDto, Resource parentResource) {
    var category = coreMapper.readResourceDoc(source, Category.class);
    category.setId(String.valueOf(source.getResourceHash()));
    if (parentDto instanceof Work work) {
      work.addContentItem(category);
    }
    if (parentDto instanceof WorkReference work) {
      work.addContentItem(category);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var category = (Category) dto;
    var resource = new Resource();
    resource.addType(CATEGORY);
    resource.setDoc(getDoc(category));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Category dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, LINK, dto.getLink());
    putProperty(map, TERM, dto.getTerm());
    putProperty(map, SOURCE, dto.getSource());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
