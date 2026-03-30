package org.folio.linked.data.mapper.dto.resource.common;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.ReferenceResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.reference.ReferenceService;

@RequiredArgsConstructor
public abstract class ReferenceMapperUnit implements SingleResourceMapperUnit {

  private final ReferenceService referenceService;

  protected ReferenceResponse toReference(Resource resource) {
    var latestResource = ensureLatestReplaced(resource);
    return new ReferenceResponse()
      .id(String.valueOf(latestResource.getId()))
      .label(latestResource.getLabel())
      .isPreferred(isFolioResource(latestResource))
      .types(latestResource.getTypes().stream().map(ResourceTypeEntity::getUri).toList());
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var reference = (Reference) dto;
    return referenceService.resolveReference(reference);
  }

  protected boolean isFolioResource(Resource resource) {
    return nonNull(resource.getFolioMetadata());
  }
}
