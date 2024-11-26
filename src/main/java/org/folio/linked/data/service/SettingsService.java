package org.folio.linked.data.service;

public interface SettingsService {

  boolean isSettingEnabled(String scope, String key, String property);
}
