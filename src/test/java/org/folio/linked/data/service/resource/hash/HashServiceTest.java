package org.folio.linked.data.service.resource.hash;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HashServiceTest {

  @InjectMocks
  private HashServiceImpl hashService;
  @Mock
  private FingerprintHashService fingerprintHashService;
  @Mock
  private ResourceModelMapper resourceModelMapper;

  @Test
  void hash_shouldThrowNpe_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    var thrown = assertThrows(NullPointerException.class, () -> hashService.hash(resource));

    // then
    assertThat(thrown.getMessage()).isEqualTo("resource is marked non-null but is null");
  }

  @Test
  void hash_shouldReturnFingerprintHashOfMappedModel_ifGivenResourceIsNotNull() {
    // given
    var resource = new Resource();
    var mapped = new org.folio.ld.dictionary.model.Resource();
    when(resourceModelMapper.toModel(resource)).thenReturn(mapped);
    var expectedHash = randomLong();
    when(fingerprintHashService.hash(mapped)).thenReturn(expectedHash);

    // when
    var result = hashService.hash(resource);

    // then
    assertThat(result).isEqualTo(expectedHash);
  }

}
