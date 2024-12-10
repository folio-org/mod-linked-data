package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = FORM, predicate = GENRE, requestDto = Reference.class)
public class GenreMapperUnit extends ReferenceMapperUnit {

  public GenreMapperUnit(ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super((genre, destination) -> {
      if (destination instanceof WorkResponse work) {
        work.addGenreReferenceItem(genre);
      }
    }, resourceMarcAuthorityService);
  }

}
