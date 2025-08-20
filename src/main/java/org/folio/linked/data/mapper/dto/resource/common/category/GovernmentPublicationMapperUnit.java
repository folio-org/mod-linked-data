package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import java.util.Optional;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = GOVERNMENT_PUBLICATION, requestDto = Category.class)
public class GovernmentPublicationMapperUnit extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "Government Publication Type";
  private static final String CATEGORY_SET_LINK = "http://id.loc.gov/vocabulary/mgovtpubtype";
  private static final String GOVERNMENT_PUBLICATION_LINK_PREFIX = "http://id.loc.gov/vocabulary/mgovtpubtype/";
  private static final String LINK_SUFFIX_G = "g";
  private static final String MARC_CODE_O = "o";

  public GovernmentPublicationMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addGovernmentPublicationItem(category);
    }
  }

  @Override
  public Optional<String> getMarcCode(String linkSuffix) {
    if (LINK_SUFFIX_G.equals(linkSuffix)) {
      return Optional.of(MARC_CODE_O);
    }
    return super.getMarcCode(linkSuffix);
  }

  @Override
  public String getLinkPrefix() {
    return GOVERNMENT_PUBLICATION_LINK_PREFIX;
  }

  @Override
  protected String getCategorySetLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return CATEGORY_SET_LINK;
  }
}
