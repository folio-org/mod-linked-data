package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static java.util.List.copyOf;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESSIBLE_AUDIO_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESSIBLE_VISUAL_MATERIAL_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ACCOMPANYING_MATERIAL_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ACCOMPANYING_TRANSCRIPTS_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.CAPTIONS_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.INTERMEDIATE_TRANSLATIONS_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.INTERTITLES_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.LIBRETTO_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGINAL_ACCOMPANYING_MATERIALS_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGINAL_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGINAL_LIBRETTO_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.SUBTITLES_OR_CAPTIONS_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.SUMMARY_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.SUNG_OR_SPOKEN_TEXT_LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.TABLE_OF_CONTENTS_LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.MarcCodeProvider;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(
  type = LANGUAGE_CATEGORY,
  predicate = {
    ACCESSIBLE_AUDIO_LANGUAGE, ACCESSIBLE_VISUAL_MATERIAL_LANGUAGE, ACCOMPANYING_MATERIAL_LANGUAGE,
    ACCOMPANYING_TRANSCRIPTS_LANGUAGE, CAPTIONS_LANGUAGE, INTERMEDIATE_TRANSLATIONS_LANGUAGE,
    INTERTITLES_LANGUAGE, LANGUAGE, LIBRETTO_LANGUAGE, ORIGINAL_ACCOMPANYING_MATERIALS_LANGUAGE,
    ORIGINAL_LANGUAGE, ORIGINAL_LIBRETTO_LANGUAGE, SUBTITLES_OR_CAPTIONS_LANGUAGE, SUMMARY_LANGUAGE,
    SUNG_OR_SPOKEN_TEXT_LANGUAGE, TABLE_OF_CONTENTS_LANGUAGE
  },
  requestDto = Language.class
)
@RequiredArgsConstructor
public class LanguageCategoryMapperUnit implements WorkSubResourceMapperUnit, MarcCodeProvider {
  private static final String LANGUAGE_LINK_PREFIX = "http://id.loc.gov/vocabulary/languages";
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource languageResource, P parentWorkDto, ResourceMappingContext mappingContext) {
    if (parentWorkDto instanceof WorkResponse workDto) {
      findExistingLanguageDto(workDto, languageResource.getId())
        .ifPresentOrElse(
          languageDto -> addTypeToLanguage(languageDto, mappingContext.predicate().getUri()),
          () -> workDto.addLanguagesItem(createLanguageDto(languageResource, mappingContext.predicate().getUri()))
        );

      // Temporary code
      if (mappingContext.predicate().getUri().equals(LANGUAGE.getUri())) {
        var category = coreMapper.toDtoWithEdges(languageResource, CategoryResponse.class, false);
        category.setId(String.valueOf(languageResource.getId()));
        workDto.addLanguageItem(category);
      }
    }
    return parentWorkDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var languageCategory = (Language) dto;
    var resource = new Resource()
            .setLabel(getFirstValue(() -> getMarcCodes(languageCategory.getLink())))
            .addTypes(LANGUAGE_CATEGORY)
            .setDoc(getDoc(languageCategory));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  @Override
  public String getLinkPrefix() {
    return LANGUAGE_LINK_PREFIX;
  }

  private void addTypeToLanguage(Language languageDto, String typeUri) {
    var currentTypes = new ArrayList<>(languageDto.getTypes());
    currentTypes.add(typeUri);
    languageDto.types(copyOf(currentTypes));
  }

  private Language createLanguageDto(Resource languageResource, String type) {
    return coreMapper.toDtoWithEdges(languageResource, Language.class, false)
      .id(String.valueOf(languageResource.getId()))
      .types(List.of(type));
  }

  private Optional<Language> findExistingLanguageDto(WorkResponse workDto, Long languageId) {
    return workDto.getLanguages()
      .stream()
      .filter(languageDto -> languageDto.getId().equals(String.valueOf(languageId)))
      .findFirst();
  }

  private JsonNode getDoc(Language dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, getMarcCodes(dto.getLink()));
    putProperty(map, TERM, dto.getTerm());
    putProperty(map, LINK, dto.getLink());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
