package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = LANGUAGE_CATEGORY, predicate = LANGUAGE, requestDto = Category.class)
@Deprecated(forRemoval = true)
public class LanguageMapperUnit extends CategoryMapperUnit {

  private static final String LANGUAGE_LINK_PREFIX = "http://id.loc.gov/vocabulary/languages";

  public LanguageMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addLanguageItem(category);
    }
  }

  @Override
  protected String getCategorySetLabel() {
    return EMPTY;
  }

  @Override
  protected String getCategorySetLink() {
    return EMPTY;
  }

  @Override
  public String getLinkPrefix() {
    return LANGUAGE_LINK_PREFIX;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var languageCategory = (Category) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(() -> getMarcCodes(languageCategory.getLink())));
    resource.addTypes(LANGUAGE_CATEGORY);
    resource.setDoc(getDoc(languageCategory));
    resource.setId(hashService.hash(resource));
    return resource;
  }
}
