package org.folio.linked.data.mapper.dto.resource.monograph;

import static org.folio.ld.dictionary.PredicateDictionary.BOOK_FORMAT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.category.CategoryMapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = BOOK_FORMAT, requestDto = Category.class)
public class BookFormatMapperUnit  extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "Book Format";
  private static final String CATEGORY_SET_LINK = "http://id.loc.gov/vocabulary/bookformat";
  private static final String BOOK_FORMAT_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/bookformat/";

  public BookFormatMapperUnit(HashService hashService, CoreMapper coreMapper) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof InstanceResponse instance) {
      instance.addBookFormatItem(category);
    }
  }

  @Override
  protected String getCategorySetLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return CATEGORY_SET_LINK;
  }

  @Override
  public String getLinkPrefix() {
    return BOOK_FORMAT_TYPE_LINK_PREFIX;
  }
}
