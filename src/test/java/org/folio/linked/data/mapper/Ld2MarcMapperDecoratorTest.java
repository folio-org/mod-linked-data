package org.folio.linked.data.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.service.resource.marc.RawMarcService;
import org.folio.marc4ld.enums.UnmappedMarcHandling;
import org.folio.marc4ld.service.ld2marc.Ld2MarcMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class Ld2MarcMapperDecoratorTest {

  @Mock
  private Ld2MarcMapper delegate;
  @Mock
  private RawMarcService rawMarcService;
  @InjectMocks
  private Ld2MarcMapperDecorator decorator;

  @Test
  void shouldEnrichResourceWithRawMarc_whenToMarcJsonIsCalled() {
    // given
    var resource = new Resource().setId(110L);
    var rawMarcContent = "raw-marc-content";
    when(rawMarcService.getRawMarc(110L)).thenReturn(Optional.of(rawMarcContent));

    // when
    decorator.toMarcJson(resource);

    // then
    var resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(delegate).toMarcJson(resourceCaptor.capture());
    var enrichedResource = resourceCaptor.getValue();

    assertThat(enrichedResource.getUnmappedMarc().getContent()).isEqualTo(rawMarcContent);
  }

  @Test
  void shouldEnrichResourceWithRawMarc_whenToMarcJsonWithHandlingIsCalled() {
    // given
    var resource = new Resource().setId(110L);
    var rawMarcContent = "raw-marc-content";
    when(rawMarcService.getRawMarc(110L)).thenReturn(Optional.of(rawMarcContent));

    // when
    decorator.toMarcJson(resource, UnmappedMarcHandling.APPEND);

    // then
    var resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(delegate).toMarcJson(resourceCaptor.capture(), eq(UnmappedMarcHandling.APPEND));
    var enrichedResource = resourceCaptor.getValue();

    assertThat(enrichedResource.getUnmappedMarc().getContent()).isEqualTo(rawMarcContent);
  }
}
