package org.folio.linked.data.mapper.monograph.inner.instance;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.MappingUtil.addMappedResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeInstanceInner;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.mapper.resource.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapperResolver;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(INSTANCE)
public class MonographInstanceMapper implements InnerResourceMapper<BibframeInstanceInner> {

  private final SubResourceMapperResolver subResourceMapperResolver;

  private final ObjectMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    addMappedResources(mapper, subResourceMapperResolver, resource, destination::addInstanceItem, Instance.class);
    return destination;
  }

  @Override
  public Resource toResource(BibframeInstanceInner dto) {
    // TODO: implement
    return null;
  }
}
