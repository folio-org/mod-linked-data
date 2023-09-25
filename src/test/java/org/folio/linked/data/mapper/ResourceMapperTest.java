package org.folio.linked.data.mapper;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.test.type.UnitTest;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMapperTest {

  @InjectMocks
  private ResourceMapperImpl resourceMapper;

  @Mock
  private KafkaMessageMapper kafkaMessageMapper;

  @Test
  void mapToIndex_shouldThrowNullPointerException_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    var thrown = assertThrows(NullPointerException.class, () -> resourceMapper.mapToIndex(resource));

    // then
    MatcherAssert.assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void mapToIndex_shouldCallKafkaMessageMapper_ifGivenResourceIsNotNull() {
    // given
    var resource = new Resource();

    // when
    resourceMapper.mapToIndex(resource);

    // then
    verify(kafkaMessageMapper).toIndex(resource);
  }

}
