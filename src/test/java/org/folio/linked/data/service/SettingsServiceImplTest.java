package org.folio.linked.data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.folio.linked.data.client.SettingsClient;
import org.folio.linked.data.domain.dto.SettingsItem;
import org.folio.linked.data.domain.dto.SettingsSearchResponse;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

  @InjectMocks
  private SettingsServiceImpl settingsService;

  @Mock
  private SettingsClient settingsClient;

  @Test
  void isSettingEnabled_shouldReturnFalse_ifSettingsAreEmpty() {
    // given
    when(settingsClient.getEntries(any()))
      .thenReturn(new ResponseEntity<>(new SettingsSearchResponse(), HttpStatus.OK));

    // when
    boolean isEnabled = settingsService.isSettingEnabled("scope", "key", "property");

    // then
    assertFalse(isEnabled);
  }

  @ParameterizedTest
  @MethodSource("settingsProvider")
  void isSettingEnabled_shouldBaseOnSettingPropertyValue(SettingsItem item, boolean expected) {
    // given
    when(settingsClient.getEntries(any()))
      .thenReturn(new ResponseEntity<>(
        new SettingsSearchResponse().addItemsItem(item), HttpStatus.OK));

    // when
    boolean isEnabled = settingsService.isSettingEnabled("scope", "key", "property");

    // then
    assertEquals(expected, isEnabled);
  }

  private static Stream<Arguments> settingsProvider() {
    return Stream.of(
      arguments(new SettingsItem().putValueItem("property", true), true),
      arguments(new SettingsItem().putValueItem("property", false), false),
      arguments(new SettingsItem().putValueItem("property", new Object()), false),
      arguments(new SettingsItem(), false)
    );
  }
}
