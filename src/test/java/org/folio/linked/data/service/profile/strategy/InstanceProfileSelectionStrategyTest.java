package org.folio.linked.data.service.profile.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.BOOK_FORMAT;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.ProfileMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
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
class InstanceProfileSelectionStrategyTest {

  @Mock
  private PreferredProfileService preferredProfileService;
  @InjectMocks
  private InstanceProfileSelectionStrategy strategy;

  @ParameterizedTest
  @CsvSource({
    "INSTANCE, true",
    "WORK, false",
    "HUB, false"
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
  void selectProfile_shouldReturnRareBooksProfileId_whenHasBookFormatEdge() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    instance.addOutgoingEdge(new ResourceEdge(instance, new Resource(), BOOK_FORMAT));

    // when
    var result = strategy.selectProfile(instance);

    // then
    assertThat(result).isEqualTo(4);
  }

  @Test
  void selectProfile_shouldReturnSerialProfileId_whenWorkIsSerial() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    var work = new Resource().addTypes(WORK, CONTINUING_RESOURCES);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));

    // when
    var result = strategy.selectProfile(instance);

    // then
    assertThat(result).isEqualTo(5);
  }

  @Test
  void selectProfile_shouldReturnUserPreferredProfileId_whenPresent() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    when(preferredProfileService.getPreferredProfiles(INSTANCE.getUri()))
      .thenReturn(List.of(new ProfileMetadata(99, "", "")));

    // when
    var result = strategy.selectProfile(instance);

    // then
    assertThat(result).isEqualTo(99);
  }

  @Test
  void selectProfile_shouldReturnMonographProfileId_whenNoOtherConditionMet() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    when(preferredProfileService.getPreferredProfiles(INSTANCE.getUri())).thenReturn(List.of());

    // when
    var result = strategy.selectProfile(instance);

    // then
    assertThat(result).isEqualTo(3);
  }
}
