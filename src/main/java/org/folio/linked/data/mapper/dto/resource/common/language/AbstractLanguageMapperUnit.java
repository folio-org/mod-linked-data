package org.folio.linked.data.mapper.dto.resource.common.language;

import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.category.MarcCodeProvider;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
public abstract class AbstractLanguageMapperUnit implements SingleResourceMapperUnit, MarcCodeProvider {
  private static final String LANGUAGE_LINK_PREFIX = "http://id.loc.gov/vocabulary/languages";

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var languageCategory = (Language) dto;
    var resource = new Resource()
      .setLabel(getFirstValue(() -> getMarcCodes(languageCategory.getLink())))
      .addTypes(LANGUAGE_CATEGORY)
      .setDoc(getDoc(languageCategory));
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  @Override
  public String getLinkPrefix() {
    return LANGUAGE_LINK_PREFIX;
  }

  protected Language convertToLanguageDto(Resource languageResource) {
    return coreMapper.toDtoWithEdges(languageResource, Language.class, false)
      .id(String.valueOf(languageResource.getId()));
  }

  private JsonNode getDoc(Language dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, getMarcCodes(dto.getLink()));
    putProperty(map, TERM, dto.getTerm());
    putProperty(map, LINK, dto.getLink());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
