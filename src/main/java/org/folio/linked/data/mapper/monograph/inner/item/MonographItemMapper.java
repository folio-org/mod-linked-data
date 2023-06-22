package org.folio.linked.data.mapper.monograph.inner.item;

import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.MappingUtil.addMappedResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeItemInner;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.mapper.resource.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.inner.sub.SubResourceMapperResolver;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(ITEM)
public class MonographItemMapper implements InnerResourceMapper<BibframeItemInner> {

  private final SubResourceMapperResolver subResourceMapperResolver;
  private final ObjectMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    addMappedResources(mapper, subResourceMapperResolver, resource, destination::addItemItem, Item.class);
    return destination;
  }

  @Override
  public Resource toResource(BibframeItemInner dto) {
    // TODO: implement
    return null;
  }
}
