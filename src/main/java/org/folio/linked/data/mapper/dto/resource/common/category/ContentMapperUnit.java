package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.linked.data.util.ResourceUtils.getSourceOrFallback;

import java.util.List;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CONTENT, requestDto = Category.class)
public class ContentMapperUnit extends CategoryMapperUnit {

  private static final String CONTENT_CATEGORY_LABEL = "rdacontent";
  private static final String CONTENT_CATEGORY_LINK = "http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent";
  private static final String CONTENT_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/contentTypes/";

  public ContentMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(hashService, coreMapper);
  }

  @Override
  protected String getCategorySetLabel() {
    return CONTENT_CATEGORY_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return CONTENT_CATEGORY_LINK;
  }

  @Override
  protected List<String> getSource(Category dto) {
    return getSourceOrFallback(dto.getSource(), CONTENT_CATEGORY_LABEL);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addContentItem(category);
    }
  }

  @Override
  public String getLinkPrefix() {
    return CONTENT_TYPE_LINK_PREFIX;
  }
}
