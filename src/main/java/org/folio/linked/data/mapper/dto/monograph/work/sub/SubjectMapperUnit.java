package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CONCEPT, predicate = SUBJECT, dtoClass = Reference.class)
public class SubjectMapperUnit extends ReferenceMapperUnit {

  public SubjectMapperUnit(ResourceRepository resourceRepository) {
    super((subject, destination) -> {
      if (destination instanceof Work work) {
        work.addSubjectsItem(subject);
      }
      if (destination instanceof WorkReference work) {
        work.addSubjectsItem(subject);
      }
    }, resourceRepository);
  }
}
