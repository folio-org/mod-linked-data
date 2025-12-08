package org.folio.linked.data.integration.rest.settings;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(STANDALONE_PROFILE)
public class SettingsServiceStandalone implements SettingsService {

  @Override
  public boolean isSettingEnabled(String scope, String key, String property) {
    return true;
  }

}
