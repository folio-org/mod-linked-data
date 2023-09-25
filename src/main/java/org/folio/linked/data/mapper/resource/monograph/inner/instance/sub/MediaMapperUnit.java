package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.CATEGORY;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.CategoryMapperUnit;
import org.folio.linked.data.service.dictionary.ResourceTypeService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = MEDIA_PRED, dtoClass = Category.class)
public class MediaMapperUnit extends CategoryMapperUnit {


  public MediaMapperUnit(CoreMapper coreMapper, ResourceTypeService resourceTypeService) {
    super(coreMapper, resourceTypeService, (category, instance) -> instance.addMediaItem(category), CATEGORY);
  }
}
