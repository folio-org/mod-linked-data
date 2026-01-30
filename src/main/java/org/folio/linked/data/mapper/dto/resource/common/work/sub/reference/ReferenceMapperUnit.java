package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.ReferenceResponse;
import org.folio.linked.data.mapper.dto.resource.common.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.reference.ReferenceService;
import org.folio.linked.data.util.ResourceUtils;

@RequiredArgsConstructor
public abstract class ReferenceMapperUnit implements WorkSubResourceMapperUnit {

  private final ReferenceService referenceService;

  protected ReferenceResponse toReference(Resource resource) {
    var latestResource = ensureLatestReplaced(resource);
    return new ReferenceResponse()
      .id(String.valueOf(latestResource.getId()))
      .label(latestResource.getLabel())
      .isPreferred(isPreferred(latestResource))
      .types(latestResource.getTypes().stream().map(ResourceTypeEntity::getUri).toList());
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var reference = (Reference) dto;
    return referenceService.resolveReference(reference);
  }

  protected boolean isPreferred(Resource resource) {
    return ResourceUtils.isPreferred(resource);
  }
}
