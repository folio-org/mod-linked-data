package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = FORM, predicate = GENRE, requestDto = Reference.class)
public class GenreMapperUnit extends ReferenceMapperUnit {

  public GenreMapperUnit(ReferenceService referenceService) {
    super((genre, destination) -> {
      if (destination instanceof WorkResponse work) {
        work.addGenreReferenceItem(genre);
      }
    }, referenceService);
  }

}
