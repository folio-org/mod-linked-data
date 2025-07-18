package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;

import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.linked.data.util.ResourceUtils;

@RequiredArgsConstructor
public class ReferenceMapperUnit implements WorkSubResourceMapperUnit {

  private final BiConsumer<Reference, Object> referenceConsumer;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    resourceToConvert = ensureLatestReplaced(resourceToConvert);
    var reference = new Reference()
      .id(String.valueOf(resourceToConvert.getId()))
      .label(resourceToConvert.getLabel())
      .isPreferred(isPreferred(resourceToConvert));
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
