package org.folio.linked.data.service.resource.marc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.RawMarcRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RawMarcServiceImplTest {

  @Mock
  private RawMarcRepository rawMarcRepository;

  @InjectMocks
  private RawMarcServiceImpl rawMarcService;

  @Test
  void testGetRawMarcForNonInstanceResource() {
    // given
    var workResource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(WORK.getUri())));

    // when
    var result = rawMarcService.getRawMarc(workResource);

    // then
    assertThat(result).isEmpty();
    verifyNoInteractions(rawMarcRepository);
  }

  @Test
  void testGetRawMarcForInstanceResource() {
    // given
    var rawMarcContent = "rawMarcContent";
    var resource = new Resource()
      .setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())))
      .setIdAndRefreshEdges(1L);
    when(rawMarcRepository.findById(1L)).thenReturn(Optional.of(new RawMarc(resource).setContent(rawMarcContent)));

    // when
    var result = rawMarcService.getRawMarc(resource);

    // then
    assertThat(result).contains(rawMarcContent);
    verify(rawMarcRepository).findById(1L);
  }

  @Test
  void testGetRawMarcById() {
    // given
    var rawMarcContent = "rawMarcContent";
    when(rawMarcRepository.findById(1L))
      .thenReturn(Optional.of(new RawMarc(new Resource()).setContent(rawMarcContent)));

    // when
    var result = rawMarcService.getRawMarc(1L);

    // then
    assertThat(result).contains(rawMarcContent);
    verify(rawMarcRepository).findById(1L);
  }

  @Test
  void testSaveRawMarcForNonInstanceResource() {
    // given
    var workResource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(WORK.getUri())));

    // when
    rawMarcService.saveRawMarc(workResource, "rawMarcContent");

    // then
    verifyNoInteractions(rawMarcRepository);
  }

  @Test
  void testSaveRawMarcForInstanceResource() {
    // given
    var instanceResource = new Resource()
      .setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())))
      .setIdAndRefreshEdges(100L);

    // when
    rawMarcService.saveRawMarc(instanceResource, "rawMarcContent");

    // then
    var rawMarcCaptor = ArgumentCaptor.forClass(RawMarc.class);
    verify(rawMarcRepository).save(rawMarcCaptor.capture());
    assertThat(rawMarcCaptor.getValue())
      .extracting(RawMarc::getContent, RawMarc::getId)
      .containsExactly("rawMarcContent", instanceResource.getId());
  }

  @Test
  void testSaveRawMarcWithNullContent() {
    // given
    var instanceResource = new Resource().setTypes(Set.of(new ResourceTypeEntity().setUri(INSTANCE.getUri())));

    // when
    rawMarcService.saveRawMarc(instanceResource, null);

    // then
    verifyNoInteractions(rawMarcRepository);
  }
}
