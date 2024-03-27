package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;

@RequiredArgsConstructor
public class ReferenceMapperUnit implements WorkSubResourceMapperUnit {

  private final BiConsumer<Reference, Object> referenceConsumer;
  private final ResourceRepository resourceRepository;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var reference = new Reference()
      .id(source.getResourceHash().toString())
      .label(source.getLabel());
    referenceConsumer.accept(reference, parentDto);
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var reference = (Reference) dto;
    return resourceRepository
      .findById(Long.parseLong(reference.getId()))
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + reference.getId() + IS_NOT_FOUND));
  }
}
