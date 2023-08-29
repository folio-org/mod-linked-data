package org.folio.linked.data.mapper.resource.monograph.inner.work;

import static org.folio.linked.data.util.BibframeConstants.WORK_2;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK_2)
public class MonographWork2MapperUnit implements InnerResourceMapperUnit {

  private final SubResourceMapper subResourceMapper;
  private final CoreMapper coreMapper;

  @Override
  public BibframeResponse toDto(Resource source, BibframeResponse destination) {
    return destination;
  }

  @Override
  public Bibframe2Response toDto(Resource resource, Bibframe2Response destination) {
    coreMapper.mapWithResources(subResourceMapper, resource, destination::addWorkItem, Work2.class);
    return destination;
  }

  @Override
  public Resource toEntity(Object innerResourceDto) {
    return null;
  }
}
