package org.folio.linked.data.integration.rest.specification;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.List;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(STANDALONE_PROFILE)
public class SpecProviderStandalone implements SpecProvider {

  @Override
  public List<SpecificationRuleDto> getSpecRules() {
    return List.of();
  }

}
