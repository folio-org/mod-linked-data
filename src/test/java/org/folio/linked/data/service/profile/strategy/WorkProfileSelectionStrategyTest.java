package org.folio.linked.data.service.profile.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.profile.PreferredProfileService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkProfileSelectionStrategyTest {

  @Mock
  private PreferredProfileService preferredProfileService;
  @InjectMocks
  private WorkProfileSelectionStrategy strategy;

  @ParameterizedTest
  @CsvSource({
    "WORK, true",
    "HUB, false",
    "INSTANCE, false"
  })
  void supports_shouldReturnExpectedResult(ResourceTypeDictionary type, boolean expected) {
    // given
    var resource = new Resource().addTypes(type);

    // when
    var result = strategy.supports(resource);

    // then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void selectProfile_shouldReturnSerialProfileId_whenResourceIsContinuingResources() {
    // given
    var resource = new Resource().addTypes(WORK, CONTINUING_RESOURCES);

    // when
    var result = strategy.selectProfile(resource);

    // then
    assertThat(result).isEqualTo(6);
  }

  @Test
  void selectProfile_shouldReturnMonographProfileId_whenResourceIsBooks() {
    // given
    var resource = new Resource().addTypes(WORK, BOOKS);

    // when
    var result = strategy.selectProfile(resource);

    // then
    assertThat(result).isEqualTo(2);
  }

  @Test
  void selectProfile_shouldReturnUserPreferredProfileId_whenPresent() {
    // given
    var resource = new Resource().addTypes(WORK);
    when(preferredProfileService.getPreferredProfiles(WORK.getUri()))
      .thenReturn(List.of(new ProfileMetadata(99, "", "")));

    // when
    var result = strategy.selectProfile(resource);

    // then
    assertThat(result).isEqualTo(99);
  }

  @Test
  void selectProfile_shouldReturnMonographProfileId_whenNoOtherConditionMet() {
    // given
    var resource = new Resource().addTypes(WORK);
    when(preferredProfileService.getPreferredProfiles(WORK.getUri())).thenReturn(List.of());

    // when
    var result = strategy.selectProfile(resource);

    // then
    assertThat(result).isEqualTo(2);
  }
}
