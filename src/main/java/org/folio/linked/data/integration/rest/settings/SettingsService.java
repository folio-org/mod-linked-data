package org.folio.linked.data.integration.rest.settings;

public interface SettingsService {

  boolean isSettingEnabled(String scope, String key, String property);
}
