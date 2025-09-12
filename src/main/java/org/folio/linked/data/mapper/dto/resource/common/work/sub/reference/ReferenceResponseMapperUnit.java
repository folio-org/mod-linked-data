
package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;

import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.ReferenceResponse;
import org.folio.linked.data.mapper.dto.resource.common.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.linked.data.util.ResourceUtils;

@RequiredArgsConstructor
public class ReferenceResponseMapperUnit implements WorkSubResourceMapperUnit {

  private final BiConsumer<ReferenceResponse, Object> referenceConsumer;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    resourceToConvert = ensureLatestReplaced(resourceToConvert);
    var reference = new ReferenceResponse()
      .id(String.valueOf(resourceToConvert.getId()))
      .label(resourceToConvert.getLabel())
      .isPreferred(isPreferred(resourceToConvert))
      .types(resourceToConvert.getTypes().stream().map(ResourceTypeEntity::getUri).toList());
    referenceConsumer.accept(reference, parentDto);
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var reference = (Reference) dto;
    return resourceMarcAuthorityService.fetchAuthorityOrCreateFromSrsRecord(reference);
  }

  protected boolean isPreferred(Resource resource) {
    return ResourceUtils.isPreferred(resource);
  }
}
