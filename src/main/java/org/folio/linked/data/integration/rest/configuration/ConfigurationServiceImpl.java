package org.folio.linked.data.integration.rest.configuration;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Config;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ConfigurationServiceImpl implements ConfigurationService {
  private static final String MODULE_NAME = "USERSBL";
  private static final String FOLIO_HOST_CONFIG_KEY = "FOLIO_HOST";
  private static final String FOLIO_HOST_DEFAULT = "http://localhost:8081";
  private final ConfigurationClient configurationClient;

  @Override
  public String getFolioHost() {
    return configurationClient.lookupConfig(MODULE_NAME, FOLIO_HOST_CONFIG_KEY)
      .getConfigs()
      .stream()
      .map(Config::getValue)
      .findFirst()
      .orElse(FOLIO_HOST_DEFAULT);
  }

}
