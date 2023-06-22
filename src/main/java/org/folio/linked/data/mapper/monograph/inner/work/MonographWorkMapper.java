package org.folio.linked.data.mapper.monograph.inner.work;

import static org.folio.linked.data.util.BibframeConstants.WORK;
import static org.folio.linked.data.util.MappingUtil.addMappedResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeWorkInner;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.mapper.resource.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapperResolver;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(WORK)
public class MonographWorkMapper implements InnerResourceMapper<BibframeWorkInner> {

  private final SubResourceMapperResolver subResourceMapperResolver;
  private final ObjectMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    addMappedResources(mapper, subResourceMapperResolver, resource, destination::addWorkItem,
      Work.class);
    return destination;
  }

  @Override
  public Resource toResource(BibframeWorkInner dto) {
    // TODO: implement
    return null;
  }
}
