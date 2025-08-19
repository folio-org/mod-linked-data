package org.folio.linked.data.mapper.dto.resource.serial;

import static org.folio.ld.dictionary.PredicateDictionary.CHARACTERISTIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;

import java.util.Optional;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.category.CategoryMapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = CATEGORY, predicate = CHARACTERISTIC, requestDto = Category.class)
public class CharacteristicMapperUnit extends CategoryMapperUnit {

  private static final String CATEGORY_SET_LABEL = "Serial Publication Type";
  private static final String SERIAL_PUB_TYPE_LINK_PREFIX = "http://id.loc.gov/vocabulary/mserialpubtype";

  public CharacteristicMapperUnit(HashService hashService, CoreMapper coreMapper) {
    super(hashService, coreMapper);
  }

  @Override
  protected void addToParent(CategoryResponse category, Object parentDto) {
    if (parentDto instanceof WorkResponse work) {
      work.addCharacteristicItem(category);
    }
  }

  @Override
  protected String getCategorySetLabel() {
    return CATEGORY_SET_LABEL;
  }

  @Override
  protected String getCategorySetLink() {
    return SERIAL_PUB_TYPE_LINK_PREFIX;
  }

  @Override
  public String getLinkPrefix() {
    return SERIAL_PUB_TYPE_LINK_PREFIX;
  }

  @Override
  public Optional<String> getMarcCode(String linkSuffix) {
    var result = switch (linkSuffix) {
      case "database" -> "d";
      case "mag" -> "g";
      case "blog" -> "h";
      case "journal" -> "j";
      case "looseleaf" -> "l";
      case "monoseries" -> "m";
      case "newspaper" -> "n";
      case "periodical" -> "p";
      case "repo" -> "r";
      case "newsletter" -> "s";
      case "direct" -> "t";
      case "web" -> "w";
      default -> null;
    };
    return Optional.ofNullable(result);
  }
}
