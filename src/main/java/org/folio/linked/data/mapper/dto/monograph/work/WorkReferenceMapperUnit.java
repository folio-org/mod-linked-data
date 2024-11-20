package org.folio.linked.data.mapper.dto.monograph.work;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PropertyDictionary.BIBLIOGRAPHY_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.IdField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.NoteMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, predicate = INSTANTIATES, requestDto = IdField.class)
public class WorkReferenceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    InstanceRequest.class,
    InstanceResponse.class
  );
  private static final Set<PropertyDictionary> SUPPORTED_NOTES = Set.of(BIBLIOGRAPHY_NOTE, LANGUAGE_NOTE, NOTE);

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;
  private final ResourceRepository resourceRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof InstanceResponse instance) {
      var workResponse = coreMapper.toDtoWithEdges(source, WorkResponse.class, false);
      workResponse.setId(String.valueOf(source.getId()));
      ofNullable(source.getDoc()).ifPresent(doc -> workResponse.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      instance.addWorkReferenceItem(workResponse);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var workIdField = (IdField) dto;
    if (nonNull(workIdField.getId())) {
      return resourceRepository.findById(Long.parseLong(workIdField.getId()))
        .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Work", workIdField.getId()));
    } else {
      throw exceptionBuilder.requiredException("Work id");
    }
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
