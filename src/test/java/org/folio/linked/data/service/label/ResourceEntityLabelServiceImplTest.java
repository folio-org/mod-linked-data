package org.folio.linked.data.service.label;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.ld.dictionary.label.LabelGeneratorService;
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
class ResourceEntityLabelServiceImplTest {

  @InjectMocks
  private ResourceEntityLabelServiceImpl resourceEntityLabelService;

  @Mock
  private LabelGeneratorService labelGeneratorService;

  @Mock
  private ResourceModelMapper resourceModelMapper;

  @Test
  void assignLabelToResource_shouldSetLabelAndAddLabelProperty() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(1L);
    var resourceModel = new org.folio.ld.dictionary.model.Resource();
    var label = "Some Label";
    when(resourceModelMapper.toModel(resource, 1)).thenReturn(resourceModel);
    when(labelGeneratorService.getLabel(resourceModel)).thenReturn(label);

    // when
    resourceEntityLabelService.assignLabelToResource(resource);

    // then
    assertThat(resource.getLabel()).isEqualTo(label);
    assertThat(getPropertyValues(resource, LABEL)).containsExactly(label);
    verify(resourceModelMapper).toModel(resource, 1);
    verify(labelGeneratorService).getLabel(resourceModel);
  }

  @Test
  void assignLabelToResource_shouldSkipWhenLabelIsNull() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(1L);
    var resourceModel = new org.folio.ld.dictionary.model.Resource();
    when(resourceModelMapper.toModel(resource, 1)).thenReturn(resourceModel);
    when(labelGeneratorService.getLabel(resourceModel)).thenReturn(null);

    // when
    resourceEntityLabelService.assignLabelToResource(resource);

    // then
    assertThat(resource.getLabel()).isEmpty();
    assertThat(getPropertyValues(resource, LABEL)).isEmpty();
    verify(resourceModelMapper).toModel(resource, 1);
    verify(labelGeneratorService).getLabel(resourceModel);
  }

  @Test
  void assignLabelToResource_shouldSkipWhenLabelIsBlank() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(1L);
    var resourceModel = new org.folio.ld.dictionary.model.Resource();
    when(resourceModelMapper.toModel(resource, 1)).thenReturn(resourceModel);
    when(labelGeneratorService.getLabel(resourceModel)).thenReturn("  ");

    // when
    resourceEntityLabelService.assignLabelToResource(resource);

    // then
    assertThat(resource.getLabel()).isEmpty();
    assertThat(getPropertyValues(resource, LABEL)).isEmpty();
    verify(resourceModelMapper).toModel(resource, 1);
    verify(labelGeneratorService).getLabel(resourceModel);
  }
}
