package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = FORM, predicate = GENRE, dtoClass = Reference.class)
public class GenreMapperUnit extends ReferenceMapperUnit {

  public GenreMapperUnit(ResourceRepository resourceRepository) {
    super((genre, destination) -> {
      if (destination instanceof Work work) {
        work.addGenreReferenceItem(genre);
      } else if (destination instanceof WorkReference workReference) {
        workReference.addGenreReferenceItem(genre);
      }
    }, resourceRepository);
  }
}
