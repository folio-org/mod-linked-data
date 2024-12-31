package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = PredicateDictionary.ILLUSTRATIONS, requestDto = Category.class)
public class IllustrationsMapperUnit extends CategoryMapperUnit {

  private static final String LABEL = "Illustrative Content";
  private static final String LINK = "http://id.loc.gov/vocabulary/millus";
  private static final String LINK_PREFIX = "http://id.loc.gov/vocabulary/millus/";

  public IllustrationsMapperUnit(HashService hashService, CoreMapper coreMapper) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof InstanceResponse instance) {
      instance.addIllustrationsItem(category);
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
