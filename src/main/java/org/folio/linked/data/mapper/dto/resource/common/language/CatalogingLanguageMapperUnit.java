package org.folio.linked.data.mapper.dto.resource.common.language;

import static org.folio.ld.dictionary.PredicateDictionary.CATALOGING_LANGUAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;

import java.util.Set;
import org.folio.linked.data.domain.dto.AdminMetadata;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = LANGUAGE_CATEGORY, predicate = CATALOGING_LANGUAGE, requestDto = Language.class)
public class CatalogingLanguageMapperUnit extends AbstractLanguageMapperUnit {

  public CatalogingLanguageMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  public <P> P toDto(Resource languageResource, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof AdminMetadata adminMetadata) {
      var languageDto = convertToLanguageDto(languageResource);
      adminMetadata.addCatalogingLanguageItem(languageDto);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(AdminMetadata.class);
  }
}
