package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import java.util.Optional;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.CategorySetMapperUnit;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = TARGET_AUDIENCE, dtoClass = Category.class)
public class TargetAudienceMapperUnit extends CategorySetMapperUnit {

  private static final String CATEGORY_SET_LABEL = "Target audience";
  private static final String TARGET_AUDIENCE_LINK_PREFIX = "http://id.loc.gov/vocabulary/maudience";


  public TargetAudienceMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected String getLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getLink() {
    return TARGET_AUDIENCE_LINK_PREFIX;
  }

  @Override
  protected void addToParent(Category category, Object parentDto) {
    if (parentDto instanceof Work work) {
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
