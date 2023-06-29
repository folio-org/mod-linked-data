package org.folio.linked.data.mapper.resource.monograph.inner.work;

import static org.folio.linked.data.util.BibframeConstants.WORK;
import static org.folio.linked.data.util.MappingUtil.addMappedResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = WORK)
public class MonographWorkMapper implements InnerResourceMapperUnit {

  private final SubResourceMapper subResourceMapper;
  private final ObjectMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    addMappedResources(mapper, subResourceMapper, resource, destination::addWorkItem, Work.class);
    return destination;
  }

  @Override
  public ResourceEdge toEntity(Object innerResourceDto, Resource parent) {
    return null;
  }
}
