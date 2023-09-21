package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_EVENT;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.CopyrightEvent;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.dictionary.ResourceTypeService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = COPYRIGHT_EVENT, predicate = COPYRIGHT_DATE_PRED, dtoClass = CopyrightEvent.class)
public class CopyrightEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final ResourceTypeService resourceTypeService;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var copyrightEvent = coreMapper.readResourceDoc(source, CopyrightEvent.class);
    copyrightEvent.setId(source.getResourceHash());
    copyrightEvent.addLabelItem(source.getLabel());
    return destination.addCopyrightItem(copyrightEvent);
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var copyrightEvent = (CopyrightEvent) dto;
    var resource = new Resource();
    resource.setLabel(getFirst(copyrightEvent.getLabel(), getFirst(copyrightEvent.getDate(), "")));
    resource.addType(resourceTypeService.get(COPYRIGHT_EVENT));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }
}
