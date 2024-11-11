package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CONCEPT, predicate = SUBJECT, requestDto = Reference.class)
public class SubjectMapperUnit extends ReferenceMapperUnit {

  public SubjectMapperUnit(ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super((subject, destination) -> {
      if (destination instanceof WorkResponse work) {
        work.addSubjectsItem(subject);
      }
    }, resourceMarcAuthorityService);
  }

}
