package org.folio.linked.data.mapper.resource.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceTitle;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.MonographInstanceMapperUnit;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MonographInstanceMapperUnitTest {

  @Mock
  private CoreMapper coreMapper;

  @Mock
  private DictionaryService<ResourceType> resourceTypeService;

  @Mock
  private SubResourceMapper mapper;

  @InjectMocks
  private MonographInstanceMapperUnit instanceMapperUnit;

  @Test
  void toEntity_shouldReturnEntityWithInstanceTitle_ifInstanceHasInstanceTitle() {
    // given
    var instanceDto = new Instance();
    var instanceTitle = new InstanceTitle().mainTitle(List.of("Instance: instance title"));
    var varTitle = new VariantTitle().mainTitle(List.of("Instance: variant title"));
    var parTitle = new ParallelTitle().mainTitle(List.of("Instance: parallel title"));
    instanceDto.setTitle(List.of(
      new ParallelTitleField().parallelTitle(parTitle),
      new InstanceTitleField().instanceTitle(instanceTitle),
      new VariantTitleField().variantTitle(varTitle)
    ));

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEqualTo("Instance: instance title");
  }

  @Test
  void toEntity_shouldReturnEntityWithParallelTitle_ifInstanceHasNoInstanceTitle() {
    // given
    var instanceDto = new Instance();
    var varTitle = new VariantTitle().mainTitle(List.of("Instance: variant title"));
    var parTitle = new ParallelTitle().mainTitle(List.of("Instance: parallel title"));
    instanceDto.setTitle(List.of(
      new VariantTitleField().variantTitle(varTitle),
      new ParallelTitleField().parallelTitle(parTitle)
    ));

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEqualTo("Instance: parallel title");
  }

  @Test
  void toEntity_shouldReturnEntityWithVariantTitle_ifInstanceHasNoOtherTitles() {
    // given
    var instanceDto = new Instance();
    var varTitle = new VariantTitle().mainTitle(List.of("Instance: variant title"));
    instanceDto.setTitle(List.of(new VariantTitleField().variantTitle(varTitle)));

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEqualTo("Instance: variant title");
  }

  @Test
  void toEntity_shouldReturnEntityWithEmptyLabel_ifInstanceHasNoTitles() {
    // given
    var instanceDto = new Instance();

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEmpty();
  }

}
