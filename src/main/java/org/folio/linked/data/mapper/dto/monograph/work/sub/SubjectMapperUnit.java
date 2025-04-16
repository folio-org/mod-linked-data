package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.linked.data.util.ResourceUtils.copyWithoutPreferred;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CONCEPT, predicate = SUBJECT, requestDto = Reference.class)
public class SubjectMapperUnit extends ReferenceMapperUnit {

  private final HashService hashService;

  public SubjectMapperUnit(ResourceMarcAuthorityService resourceMarcAuthorityService,
                           HashService hashService) {
    super((subject, destination) -> {
      if (destination instanceof WorkResponse work) {
        work.addSubjectsItem(subject);
      }
    }, resourceMarcAuthorityService);
    this.hashService = hashService;
  }

  @Override
  public <P> P toDto(Resource concept, P parentDto, Resource parentResource) {
    var subject = concept.getOutgoingEdges().iterator().next().getTarget();
    return super.toDto(subject, parentDto, parentResource);
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var subject = super.toEntity(dto, parentEntity);
    return buildConcept(subject);
  }

  private Resource buildConcept(Resource subject) {
    var concept = new Resource()
      .setLabel(subject.getLabel())
      .setDoc(copyWithoutPreferred(subject))
      .addTypes(CONCEPT);
    subject.getTypes().forEach(concept::addType);
    concept.addOutgoingEdge(new ResourceEdge(concept, subject, FOCUS));
    concept.setId(hashService.hash(concept));
    return concept;
  }

}
