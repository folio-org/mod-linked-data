package org.folio.linked.data.mapper.resource.monograph.inner.item;

import static org.folio.linked.data.util.BibframeConstants.ITEM;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ITEM)
public class MonographItemMapperUnit implements InnerResourceMapperUnit {

  private final SubResourceMapper subResourceMapper;
  private final CoreMapper coreMapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    coreMapper.addMappedResources(subResourceMapper, resource, destination::addItemItem, Item.class);
    return destination;
  }

  @Override
  public ResourceEdge toEntity(Object innerResourceDto, Resource parent) {
    return null;
  }
}
