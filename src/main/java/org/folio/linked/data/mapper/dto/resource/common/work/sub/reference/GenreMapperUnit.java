package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;

import java.util.Set;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = FORM, predicate = GENRE, requestDto = Reference.class)
public class GenreMapperUnit extends ReferenceMapperUnit {

  public GenreMapperUnit(ReferenceService referenceService) {
    super(referenceService);
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workDto) {
      var reference = toReference(resourceToConvert);
      workDto.addGenreReferenceItem(reference);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(WorkRequest.class, WorkResponse.class);
  }
}
