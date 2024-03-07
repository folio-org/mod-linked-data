package org.folio.linked.data.mapper.resource.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.common.CategoryMapperUnit;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = MEDIA, dtoClass = Category.class)
public class MediaMapperUnit extends CategoryMapperUnit {

  public MediaMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, (category, destination) -> {
      if (destination instanceof Instance instance) {
        instance.addMediaItem(category);
      }
    }, CATEGORY);
  }
}
