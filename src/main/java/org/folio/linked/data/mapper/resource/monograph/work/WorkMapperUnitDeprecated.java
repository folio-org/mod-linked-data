package org.folio.linked.data.mapper.resource.monograph.work;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PropertyDictionary.BIBLIOGRAPHY_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.common.NoteMapper;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

/**
 * Temporary support of current ui.
 *
 * @deprecated To be removed.
 *
 */
@Deprecated(forRemoval = true)
@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, predicate = INSTANTIATES, dtoClass = Work.class)
public class WorkMapperUnitDeprecated implements InstanceSubResourceMapperUnit {

  private static final Set<PropertyDictionary> SUPPORTED_NOTES = Set.of(BIBLIOGRAPHY_NOTE, LANGUAGE_NOTE, NOTE);
  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;

  @Override
  public <D> D toDto(Resource source, D parentDto, Resource parentResource) {
    Consumer<Work> workConsumer = work -> handleMappedWork(source, parentDto, work);
    coreMapper.mapToDtoWithEdges(source, workConsumer, Work.class);
    return parentDto;
  }

  private <D> void handleMappedWork(Resource source, D destination, Work work) {
    work.setId(String.valueOf(source.getResourceHash()));
    ofNullable(source.getDoc()).ifPresent(doc -> work.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
    if (destination instanceof Instance instance) {
      instance.addInstantiatesItem(work);
    }
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var workDto = (Work) dto;
    var work = new Resource();
    work.addType(WORK);
    work.setDoc(getDoc(workDto));
    coreMapper.toOutgoingEdges(workDto.getClassification(), work, CLASSIFICATION, Work.class);
    coreMapper.toOutgoingEdges(workDto.getContent(), work, CONTENT, Work.class);
    coreMapper.toOutgoingEdges(workDto.getSubjects(), work, SUBJECT, Work.class);
    coreMapper.toOutgoingEdges(workDto.getCreator(), work, CREATOR, Work.class);
    coreMapper.toOutgoingEdges(workDto.getContributor(), work, CONTRIBUTOR, Work.class);
    work.setResourceHash(coreMapper.hash(work));
    return work;
  }

  private JsonNode getDoc(Work dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, RESPONSIBILITY_STATEMENT, dto.getResponsibiltyStatement());
    putProperty(map, TARGET_AUDIENCE, dto.getTargetAudience());
    putProperty(map, LANGUAGE, dto.getLanguage());
    putProperty(map, SUMMARY, dto.getSummary());
    putProperty(map, TABLE_OF_CONTENTS, dto.getTableOfContents());
    noteMapper.putNotes(dto.getNotes(), map);
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Collections.singleton(Instance.class);
  }
}
