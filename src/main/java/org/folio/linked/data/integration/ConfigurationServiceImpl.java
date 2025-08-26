package org.folio.linked.data.integration;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.client.ConfigurationClient;
import org.folio.linked.data.integration.model.Config;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {
  private static final String MODULE_NAME = "USERSBL";
  private static final String FOLIO_HOST_CONFIG_KEY = "FOLIO_HOST";
  private static final String FOLIO_HOST_DEFAULT = "http://localhost:8081";
  private final ConfigurationClient configurationClient;

  @Override
  public String getFolioHost() {
    return configurationClient.lookupConfigByModuleName(MODULE_NAME)
      .getConfigs()
      .stream()
      .filter(c -> FOLIO_HOST_CONFIG_KEY.equals(c.getCode()))
      .map(Config::getValue)
      .findFirst()
      .orElse(FOLIO_HOST_DEFAULT);
  }

}
