package org.folio.linked.data.mapper.dto.resource.common.language;

import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;

import java.util.Set;
import org.folio.linked.data.domain.dto.HubRequest;
import org.folio.linked.data.domain.dto.HubResponse;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = LANGUAGE_CATEGORY, predicate = LANGUAGE, requestDto = Language.class)
public class HubLanguageMapperUnit extends AbstractLanguageMapperUnit {

  public HubLanguageMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  public <P> P toDto(Resource languageResource, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof HubResponse hubResponse) {
      var languageDto = convertToLanguageDto(languageResource);
      hubResponse.addLanguagesItem(languageDto);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(HubRequest.class, HubResponse.class);
  }
}
