package org.folio.linked.data.mapper.dto.resource.common.category;

import static org.folio.ld.dictionary.PredicateDictionary.TARGET_AUDIENCE;
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
@MapperUnit(type = CATEGORY, predicate = TARGET_AUDIENCE, requestDto = Category.class)
public class TargetAudienceMapperUnit extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "Target audience";
  private static final String TARGET_AUDIENCE_LINK_PREFIX = "http://id.loc.gov/vocabulary/maudience";


  public TargetAudienceMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(hashService, coreMapper);
  }

  @Override
  protected String getCategorySetLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return TARGET_AUDIENCE_LINK_PREFIX;
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addTargetAudienceItem(category);
    }
  }

  @Override
  public Optional<String> getMarcCode(String linkSuffix) {
    var result = switch (linkSuffix) {
      case "pre" -> "a";
      case "pri" -> "b";
      case "pad" -> "c";
      case "ado" -> "d";
      case "adu" -> "e";
      case "spe" -> "f";
      case "gen" -> "g";
      case "juv" -> "j";
      default -> null;
    };
    return Optional.ofNullable(result);
  }

  @Override
  public String getLinkPrefix() {
    return TARGET_AUDIENCE_LINK_PREFIX;
  }
}
