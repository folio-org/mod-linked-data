package org.folio.linked.data.mapper.monograph;

import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.mapper.monograph.inner.instance.MonographInstanceMapper;
import org.folio.linked.data.mapper.monograph.inner.item.MonographItemMapper;
import org.folio.linked.data.mapper.monograph.inner.work.MonographWorkMapper;
import org.folio.linked.data.mapper.resource.BibframeProfiledMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.mapper.resource.inner.InnerResourceMapperResolver;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(MONOGRAPH)
public class MonographMapper implements BibframeProfiledMapper {

  private final InnerResourceMapperResolver innerResourceMapperResolver;
  private final MonographInstanceMapper instanceMapper;
  private final MonographItemMapper itemMapper;
  private final MonographWorkMapper workMapper;

  @Override
  public Resource toResource(BibframeCreateRequest bibframeCreateRequest) {
    var bibframe = new Resource();
    // TODO implement
    //bibframe.setType(bibframeCreateRequest.getProfile());
    List<Resource> instances = bibframeCreateRequest.getInstance().stream()
      .map(instanceMapper::toResource)
      .collect(Collectors.toList());
    List<Resource> items = bibframeCreateRequest.getItem().stream()
      .map(itemMapper::toResource)
      .collect(Collectors.toList());
    List<Resource> works = bibframeCreateRequest.getWork().stream()
      .map(workMapper::toResource)
      .collect(Collectors.toList());
    return bibframe;
  }

  @Override
  public BibframeResponse toResponseDto(Resource resource) {
    var response = new BibframeResponse()
      .id(resource.getResourceHash());

    resource.getOutgoingEdges().stream()
      .map(ResourceEdge::getTarget)
      .forEach(r -> innerResourceMapperResolver.getMapper(r.getType().getSimpleLabel()).toDto(r, response));
    return response;
  }

}
