package org.folio.linked.data.mapper.resource.monograph.inner.item;

import static org.folio.linked.data.util.Bibframe2Constants.ITEM_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ITEM_URL)
public class MonographItem2MapperUnit implements InnerResourceMapperUnit {

  private final SubResourceMapper subResourceMapper;
  private final CoreMapper coreMapper;

  @Override
  public ResourceDto toDto(Resource source, ResourceDto destination) {
    return destination;
  }

  @Override
  public Bibframe2Response toDto(Resource resource, Bibframe2Response destination) {
    coreMapper.mapWithResources(subResourceMapper, resource, destination::addItemItem, Item2.class);
    return destination;
  }

  @Override
  public Resource toEntity(Object innerResourceDto) {
    return null;
  }
}
