package org.folio.linked.data.test.resource;

import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.linked.data.validation.dto.LccnPatternValidator;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;

@UtilityClass
public class ResourceSpecUtil {

  public static SpecificationDtoCollection createSpecifications(UUID specRuleId) {
    return new SpecificationDtoCollection().specifications(List.of(new SpecificationDto().id(specRuleId)));
  }

  public static SpecificationRuleDtoCollection createSpecRules() {
    return new SpecificationRuleDtoCollection()
      .rules(
        List.of(new SpecificationRuleDto()
          .code(LccnPatternValidator.CODE)
          .enabled(true))
      );
  }
}
