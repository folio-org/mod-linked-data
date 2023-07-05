package org.folio.linked.data.mapper.resource.monograph.inner.work;

import static org.folio.linked.data.util.BibframeConstants.WORK;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK)
public class MonographWorkMapperUnit implements InnerResourceMapperUnit {

  private final SubResourceMapper subResourceMapper;
  private final CoreMapper coreMapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    coreMapper.addMappedResources(subResourceMapper, resource, destination::addWorkItem, Work.class);
    return destination;
  }

  @Override
  public Resource toEntity(Object innerResourceDto) {
    return null;
  }
}
