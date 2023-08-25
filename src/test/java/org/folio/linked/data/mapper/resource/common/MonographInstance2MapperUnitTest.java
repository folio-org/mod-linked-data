package org.folio.linked.data.mapper.resource.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.InstanceTitle2;
import org.folio.linked.data.domain.dto.InstanceTitleField2;
import org.folio.linked.data.domain.dto.ParallelTitle2;
import org.folio.linked.data.domain.dto.ParallelTitleField2;
import org.folio.linked.data.domain.dto.VariantTitle2;
import org.folio.linked.data.domain.dto.VariantTitleField2;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.MonographInstance2MapperUnit;
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
class MonographInstance2MapperUnitTest {

  @Mock
  private CoreMapper coreMapper;

  @Mock
  private DictionaryService<ResourceType> resourceTypeService;

  @Mock
  private SubResourceMapper mapper;

  @InjectMocks
  private MonographInstance2MapperUnit instanceMapperUnit;

  @Test
  void toEntity_shouldReturnEntityWithInstanceTitle_ifInstanceHasInstanceTitle() {
    // given
    var instanceDto = new Instance2();
    var instanceTitle = new InstanceTitle2().mainTitle(List.of("Instance: instance title"));
    var varTitle = new VariantTitle2().mainTitle(List.of("Instance: variant title"));
    var parTitle = new ParallelTitle2().mainTitle(List.of("Instance: parallel title"));
    instanceDto.setTitle(List.of(
      new ParallelTitleField2().parallelTitle(parTitle),
      new InstanceTitleField2().instanceTitle(instanceTitle),
      new VariantTitleField2().variantTitle(varTitle)
    ));

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEqualTo("Instance: instance title");
  }

  @Test
  void toEntity_shouldReturnEntityWithParallelTitle_ifInstanceHasNoInstanceTitle() {
    // given
    var instanceDto = new Instance2();
    var varTitle = new VariantTitle2().mainTitle(List.of("Instance: variant title"));
    var parTitle = new ParallelTitle2().mainTitle(List.of("Instance: parallel title"));
    instanceDto.setTitle(List.of(
      new VariantTitleField2().variantTitle(varTitle),
      new ParallelTitleField2().parallelTitle(parTitle)
    ));

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEqualTo("Instance: parallel title");
  }

  @Test
  void toEntity_shouldReturnEntityWithVariantTitle_ifInstanceHasNoOtherTitles() {
    // given
    var instanceDto = new Instance2();
    var varTitle = new VariantTitle2().mainTitle(List.of("Instance: variant title"));
    instanceDto.setTitle(List.of(new VariantTitleField2().variantTitle(varTitle)));

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEqualTo("Instance: variant title");
  }

  @Test
  void toEntity_shouldReturnEntityWithEmptyLabel_ifInstanceHasNoTitles() {
    // given
    var instanceDto = new Instance2();

    // when
    var instance = instanceMapperUnit.toEntity(instanceDto);

    // then
    assertThat(instance.getLabel()).isEmpty();
  }

}
