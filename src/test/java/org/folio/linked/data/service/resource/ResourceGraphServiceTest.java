package org.folio.linked.data.service.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceGraphServiceTest {

  @InjectMocks
  private ResourceGraphServiceImpl resourceGraphService;

  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceEdgeRepository edgeRepo;
  @Mock
  private ResourceDtoMapper resourceDtoMapper;

  @Test
  void getResourceGraph_shouldReturnResourceGraphDto_whenResourceExists() {
    //given
    var id = randomLong();
    var resource = new Resource().setId(id);
    var expectedResourceGraphDto = new ResourceGraphDto().id(String.valueOf(id));

    when(resourceRepo.findById(id)).thenReturn(Optional.of(resource));
    when(resourceDtoMapper.toResourceGraphDto(resource)).thenReturn(expectedResourceGraphDto);

    //when
    var resourceGraphDto = resourceGraphService.getResourceGraph(id);

    //then
    assertThat(expectedResourceGraphDto).isEqualTo(resourceGraphDto);
  }

  @Test
  void getResourceGraph_shouldThrowNotFoundException_whenResourceDoesNotExist() {
    // given
    var id = randomLong();

    when(resourceRepo.findById(id)).thenReturn(Optional.empty());

    // when
    var thrown = assertThrows(NotFoundException.class, () -> resourceGraphService.getResourceGraph(id));

    // then
    assertThat(thrown.getMessage()).isEqualTo(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
  }

}
