package org.folio.linked.data.mapper.resource.monograph;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.WORK;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.mapper.resource.common.BibframeProfiledMapperUnit;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = MONOGRAPH)
public class MonographMapperUnit implements BibframeProfiledMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final InnerResourceMapper innerResourceMapper;
  private final CoreMapper coreMapper;

  @Override
  public Resource toResource(BibframeRequest bibframeRequest) {
    var bibframe = new Resource();
    bibframe.setLabel(MONOGRAPH);
    bibframe.setType(resourceTypeService.get(MONOGRAPH));
    addResources(bibframeRequest.getWork(), WORK, bibframe);
    addResources(bibframeRequest.getInstance(), INSTANCE, bibframe);
    addResources(bibframeRequest.getItem(), ITEM, bibframe);
    bibframe.setResourceHash(coreMapper.hash(bibframe));
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
