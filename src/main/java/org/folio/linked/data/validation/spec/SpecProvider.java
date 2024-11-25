package org.folio.linked.data.validation.spec;

import java.util.List;
import org.folio.rspec.domain.dto.SpecificationRuleDto;

public interface SpecProvider {

  List<SpecificationRuleDto> getSpecRules();
}
