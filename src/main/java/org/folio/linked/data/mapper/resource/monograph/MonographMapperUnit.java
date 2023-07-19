package org.folio.linked.data.mapper.resource.monograph;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.BibframeConstants.ITEM_URL;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.WORK;
import static org.folio.linked.data.util.BibframeConstants.WORK_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.ProfiledMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = MONOGRAPH)
public class MonographMapperUnit implements ProfiledMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final InnerResourceMapper innerMapper;
  private final CoreMapper coreMapper;

  @Override
  public Resource toEntity(BibframeRequest bibframeRequest) {
    var bibframe = new Resource();
    bibframe.setType(resourceTypeService.get(MONOGRAPH));
    coreMapper.mapResourceEdges(bibframeRequest.getWork(), bibframe, WORK, WORK_URL, innerMapper::toEntity);
    coreMapper.mapResourceEdges(bibframeRequest.getInstance(), bibframe, INSTANCE, INSTANCE_URL, innerMapper::toEntity);
    coreMapper.mapResourceEdges(bibframeRequest.getItem(), bibframe, ITEM, ITEM_URL, innerMapper::toEntity);
    bibframe.setResourceHash(coreMapper.hash(bibframe));
    bibframe.setLabel(getInstanceLabel(bibframe));
    return bibframe;
  }

  private String getInstanceLabel(Resource  bibframe) {
    return bibframe.getOutgoingEdges().stream()
      .filter(re -> INSTANCE_URL.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(Resource::getLabel)
      .findFirst().orElse("");
  }

  @Override
  public BibframeResponse toDto(Resource resource) {
    var response = new BibframeResponse().id(resource.getResourceHash());
    resource.getOutgoingEdges().stream()
      .map(ResourceEdge::getTarget)
      .forEach(r -> innerMapper.toDto(r, response));
    return response;
  }

}
