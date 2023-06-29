package org.folio.linked.data.mapper.resource.monograph;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.WORK;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.mapper.resource.common.BibframeProfiledMapperUnit;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = MONOGRAPH)
public class MonographMapper implements BibframeProfiledMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final InnerResourceMapper innerResourceMapper;
  private final CommonMapper commonMapper;

  @Override
  public Resource toResource(BibframeCreateRequest bibframeCreateRequest) {
    var bibframe = new Resource();
    bibframe.setLabel(MONOGRAPH);
    bibframe.setType(resourceTypeService.get(MONOGRAPH));
    addResources(bibframeCreateRequest.getWork(), WORK, bibframe);
    addResources(bibframeCreateRequest.getInstance(), INSTANCE, bibframe);
    addResources(bibframeCreateRequest.getItem(), ITEM, bibframe);
    bibframe.setResourceHash(commonMapper.hash(bibframe));
    return bibframe;
  }

  @Override
  public BibframeResponse toResponseDto(Resource resource) {
    var response = new BibframeResponse()
      .id(resource.getResourceHash());
    resource.getOutgoingEdges().stream()
      .map(ResourceEdge::getTarget)
      .forEach(r -> innerResourceMapper.toDto(r, response));
    return response;
  }

  private <T> void addResources(List<T> dtoInnerResources, String innerResourceType, Resource bibframe) {
    dtoInnerResources.stream()
      .map(bwi -> innerResourceMapper.toEntity(bwi, innerResourceType, bibframe))
      .forEach(bibframe.getOutgoingEdges()::add);
  }

}
