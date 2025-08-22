package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATIONS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import java.util.List;
import java.util.Optional;
import org.folio.ld.dictionary.specific.IllustrationDictionary;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = ILLUSTRATIONS, requestDto = Category.class)
public class IllustrationsMapperUnit extends CategoryMapperUnit {

  private static final String LABEL = "Illustrative Content";
  private static final String LINK = "http://id.loc.gov/vocabulary/millus";
  private static final String LINK_PREFIX = "http://id.loc.gov/vocabulary/millus/";

  public IllustrationsMapperUnit(HashService hashService, CoreMapper coreMapper) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addIllustrationsItem(category);
    }
  }

  @Override
  protected String getCategorySetLabel() {
    return LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return LINK;
  }

  @Override
  public String getLinkPrefix() {
    return LINK_PREFIX;
  }

  @Override
  public List<String> getMarcCodes(List<String> links) {
    return links.stream()
      .map(IllustrationDictionary::getCode)
      .flatMap(Optional::stream)
      .map(String::valueOf)
      .toList();
  }
}
