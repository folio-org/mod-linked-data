package org.folio.linked.data.service.resource.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.ResourceGraphDtoMapper;
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
class ResourceGraphServiceImplTest {

  @InjectMocks
  private ResourceGraphServiceImpl resourceGraphService;

  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceEdgeRepository edgeRepo;
  @Mock
  private ResourceGraphDtoMapper resourceDtoMapper;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;

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
    var expectedException = emptyRequestProcessingException();
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class, () -> resourceGraphService.getResourceGraph(id));

    // then
    assertThat(thrown).isEqualTo(expectedException);
  }

}
