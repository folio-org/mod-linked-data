package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;

import java.util.Set;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.ReferenceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.reference.ReferenceService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CONCEPT, predicate = SUBJECT, requestDto = Reference.class)
public class SubjectMapperUnit extends ReferenceMapperUnit {

  private final HashService hashService;

  public SubjectMapperUnit(ReferenceService referenceService,
                           HashService hashService) {
    super(referenceService);
    this.hashService = hashService;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var subject = super.toEntity(dto, parentEntity);
    return subject.isOfType(CONCEPT) ? subject : wrapWithConcept(subject);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(WorkRequest.class, WorkResponse.class);
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workDto) {
      var reference = toReference(resourceToConvert);
      workDto.addSubjectsItem(reference);
    }
    return parentDto;
  }

  @Override
  public boolean isFolioResource(Resource resource) {
    return super.isFolioResource(resource)
      || hasNoSubFocusEdges(resource) && isFocusEdgeFolioResource(resource);
  }

  private boolean hasNoSubFocusEdges(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .map(edge -> edge.getPredicate().getUri())
      .noneMatch(predicate -> predicate.equals(SUB_FOCUS.getUri()));
  }

  private boolean isFocusEdgeFolioResource(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(FOCUS.getUri()))
      .findFirst()
      .map(ResourceEdge::getTarget)
      .map(super::isFolioResource)
      .orElse(false);
  }

  private Resource wrapWithConcept(Resource subject) {
    var concept = new Resource()
      .setLabel(subject.getLabel())
      .setDoc(subject.getDoc().deepCopy())
      .addTypes(CONCEPT);
    subject.getTypes().forEach(concept::addType);
    concept.addOutgoingEdge(new ResourceEdge(concept, subject, FOCUS));
    concept.setIdAndRefreshEdges(hashService.hash(concept));
    return concept;
  }

}
