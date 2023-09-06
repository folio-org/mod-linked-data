package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.MEDIA;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;

import org.folio.linked.data.domain.dto.Triple;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.TripleMapperUnit;
import org.folio.linked.data.service.dictionary.ResourceTypeService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = MEDIA, predicate = MEDIA_PRED, dtoClass = Triple.class)
public class MediaMapperUnit extends TripleMapperUnit {


  public MediaMapperUnit(CoreMapper coreMapper, ResourceTypeService resourceTypeService) {
    super(coreMapper, resourceTypeService, (triple, instance) -> instance.addMediaItem(triple), MEDIA);
  }
}
