package org.folio.linked.data.mapper.resource.monograph.inner.item;

import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.MappingUtil.addMappedResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = ITEM)
public class MonographItemMapper implements InnerResourceMapperUnit {

  private final ObjectMapper mapper;
  @Autowired
  private SubResourceMapper subResourceMapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    addMappedResources(mapper, subResourceMapper, resource, destination::addItemItem, Item.class);
    return destination;
  }

  @Override
  public ResourceEdge toEntity(Object innerResourceDto, Resource parent) {
    return null;
  }
}
