package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import java.util.Optional;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategoryMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = GOVERNMENT_PUBLICATION, dtoClass = Category.class)
public class GovernmentPublicationMapperUnit extends CategoryMapperUnit {

  private static final String GOVERNMENT_PUBLICATION_LINK_PREFIX = "http://id.loc.gov/vocabulary/mgovtpubtype/";
  private static final String LINK_SUFFIX_G = "g";
  private static final String MARC_CODE_O = "o";

  public GovernmentPublicationMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService, CATEGORY);
  }

  @Override
  protected void addToParent(Category category, Object parentDto) {
    if (parentDto instanceof Work work) {
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
}
