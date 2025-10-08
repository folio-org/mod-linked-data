package org.folio.linked.data.mapper.dto.resource.common.work.sub;

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
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;

import java.util.List;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.LanguageWithType;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.language.AbstractLanguageMapperUnit;
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
public class WorkLanguageMapperUnit extends AbstractLanguageMapperUnit implements WorkSubResourceMapperUnit {

  public WorkLanguageMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  public <P> P toDto(Resource languageResource, P parentWorkDto, ResourceMappingContext mappingContext) {
    if (parentWorkDto instanceof WorkResponse workDto) {
      workDto.addLanguagesItem(createLanguageDto(languageResource, mappingContext.predicate().getUri()));
    }
    return parentWorkDto;
  }

  private LanguageWithType createLanguageDto(Resource languageResource, String type) {
    return new LanguageWithType()
      .addCodesItem(convertToLanguageDto(languageResource))
      .types(List.of(type));
  }
}
