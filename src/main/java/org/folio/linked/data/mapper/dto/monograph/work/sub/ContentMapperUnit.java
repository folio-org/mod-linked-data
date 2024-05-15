package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategorySetMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CONTENT, dtoClass = Category.class)
public class ContentMapperUnit extends CategorySetMapperUnit {

  private static final String CATEGORY_SET_LABEL = "rdacontent";
  private static final String CATEGORY_SET_LINK = "http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent";
  private static final String CONTENT_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/contentTypes/";

  public ContentMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected String getLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getLink() {
    return CATEGORY_SET_LINK;
  }

  @Override
  protected void addToParent(Category category, Object parentDto) {
    if (parentDto instanceof Work work) {
      work.addContentItem(category);
    }
  }

  @Override
  public String getLinkPrefix() {
    return CONTENT_TYPE_LINK_PREFIX;
  }
}
