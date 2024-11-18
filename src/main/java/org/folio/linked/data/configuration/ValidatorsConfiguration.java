package org.folio.linked.data.configuration;

import static org.folio.linked.data.util.Constants.Validators.VALIDATOR_CHAIN_LD;

import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.service.validation.LdValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorsConfiguration {

  @Bean(VALIDATOR_CHAIN_LD)
  public LdValidator<ResourceRequestDto> resourceSaveValidatorsChain(LdValidator<ResourceRequestDto> lccnValidator) {
    // in future task we can filter out disabled validators here
    return lccnValidator;
  }
}
