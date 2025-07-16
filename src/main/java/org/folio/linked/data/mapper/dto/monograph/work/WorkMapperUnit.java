package org.folio.linked.data.mapper.dto.monograph.work;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATIONS;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.IS_PART_OF;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGIN_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.AWARDS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.BIBLIOGRAPHY_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_END;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_START;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.TopResourceMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.NoteMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, requestDto = WorkField.class)
public class WorkMapperUnit extends TopResourceMapperUnit {

  public static final Set<PropertyDictionary> SUPPORTED_NOTES = Set.of(AWARDS_NOTE, BIBLIOGRAPHY_NOTE, LANGUAGE_NOTE,
    NOTE);

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;
  private final HashService hashService;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof ResourceResponseDto resourceDto) {
      var work = coreMapper.toDtoWithEdges(resourceToConvert, WorkResponse.class, true);
      work.setId(String.valueOf(resourceToConvert.getId()));
      ofNullable(resourceToConvert.getDoc()).ifPresent(doc -> work.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      resourceDto.setResource(new WorkResponseField().work(work));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var workDto = ((WorkField) dto).getWork();
    var work = new Resource();
    work.addTypes(WORK);
    work.setDoc(getDoc(workDto));
    work.setLabel(getFirstValue(() -> getPrimaryMainTitles(workDto.getTitle())));
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getTitle(), TITLE);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getClassification(), CLASSIFICATION);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getContent(), CONTENT);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getSubjects(), SUBJECT);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getCreatorReference(), CREATOR);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getContributorReference(), CONTRIBUTOR);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getGeographicCoverageReference(), GEOGRAPHIC_COVERAGE);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getGenreReference(), GENRE);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getGovernmentPublication(), GOVERNMENT_PUBLICATION);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getOriginPlace(), ORIGIN_PLACE);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getDissertation(), DISSERTATION);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getTargetAudience(), TARGET_AUDIENCE);
    // TODO (MODLD-783) - Remove the following line after UI is updated to use new "_languages" property
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getLanguage(), LANGUAGE);
    coreMapper.addIncomingEdges(work, WorkRequest.class, workDto.getInstanceReference(), INSTANTIATES);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getIllustrations(), ILLUSTRATIONS);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getSupplementaryContent(), SUPPLEMENTARY_CONTENT);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, workDto.getPartOfSeries(), IS_PART_OF);
    groupLanguagesByType(workDto.getLanguages())
      .forEach((type, languages) -> coreMapper.addOutgoingEdges(work, WorkRequest.class, languages, type));

    work.setId(hashService.hash(work));
    return work;
  }

  private JsonNode getDoc(WorkRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, SUMMARY, dto.getSummary());
    putProperty(map, TABLE_OF_CONTENTS, dto.getTableOfContents());
    putProperty(map, DATE_START, dto.getDateStart());
    putProperty(map, DATE_END, dto.getDateEnd());
    noteMapper.putNotes(dto.getNotes(), map);
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private Map<PredicateDictionary, List<Language>> groupLanguagesByType(List<Language> languageDtos) {
    Map<PredicateDictionary, List<Language>> result = new EnumMap<>(PredicateDictionary.class);
    for (var lang : languageDtos) {
      for (var type : lang.getTypes()) {
        var predicate = PredicateDictionary.fromUri(type)
          .orElseThrow(() -> exceptionBuilder.badRequestException("Invalid language type: " + type, "Bad request"));
        result.computeIfAbsent(predicate, k -> new ArrayList<>()).add(lang);
      }
    }
    return result;
  }
}
