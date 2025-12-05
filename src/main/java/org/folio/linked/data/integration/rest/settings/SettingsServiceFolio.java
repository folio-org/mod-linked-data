package org.folio.linked.data.integration.rest.settings;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.SettingsItem;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class SettingsServiceFolio implements SettingsService {

  private static final String SCOPE_AND_KEY_PATTERN = "(scope==%s and key==%s)";

  private final SettingsClient settingsClient;

  @Override
  public boolean isSettingEnabled(String scope, String key, String property) {
    return getSetting(scope, key)
      .map(item -> isPropertyEnabled(item, property))
      .orElse(false);
  }

  private Optional<SettingsItem> getSetting(String scope, String key) {
    return ofNullable(settingsClient.getEntries(buildQuery(scope, key)))
      .map(ResponseEntity::getBody)
      .flatMap(body -> body.getItems().stream().findFirst());
  }

  private boolean isPropertyEnabled(SettingsItem item, String property) {
    var propertyValue = item.getValue().getOrDefault(property, false);
    return propertyValue instanceof Boolean value && value;
  }

  private String buildQuery(String scope, String key) {
    return SCOPE_AND_KEY_PATTERN.formatted(scope, key);
  }
}
