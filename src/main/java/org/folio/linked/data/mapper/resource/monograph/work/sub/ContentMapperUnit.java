package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.resource.common.CategoryMapperUnit;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CONTENT, dtoClass = Category.class)
public class ContentMapperUnit extends CategoryMapperUnit {

  public ContentMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, (category, destination) -> {
      if (destination instanceof Work work) {
        return work.addContentItem(category);
      }
      if (destination instanceof WorkReference work) {
        return work.addContentItem(category);
      }
      return null;
    }, CATEGORY);
  }
}
