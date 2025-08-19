package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = SUPPLEMENTARY_CONTENT, requestDto = Category.class)
public class WorkSupplementaryContentMapperUnit extends CategoryMapperUnit {

  private static final String LABEL = "Supplementary Content";
  private static final String LINK = "http://id.loc.gov/vocabulary/msupplcont";
  private static final String LINK_PREFIX = "http://id.loc.gov/vocabulary/msupplcont/";

  public WorkSupplementaryContentMapperUnit(HashService hashService, CoreMapper coreMapper) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addSupplementaryContentItem(category);
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
}
