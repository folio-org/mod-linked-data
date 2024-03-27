package org.folio.linked.data.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.StreamSupport;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.dictionary.PredicateMapper;
import org.folio.linked.data.mapper.dictionary.ResourceTypeMapper;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.PredicateRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DictionaryServiceImplTest {

  @Mock
  private PredicateRepository predicateRepository;
  @Mock
  private ResourceTypeRepository resourceTypeRepository;
  @Mock
  private PredicateMapper predicateMapper;
  @Mock
  private ResourceTypeMapper resourceTypeMapper;

  @InjectMocks
  private DictionaryServiceImpl dictionaryService;

  @Test
  void shouldInitPredicates() {
    //given
    var entityMock = mock(PredicateEntity.class);
    when(predicateMapper.toEntity(any(PredicateDictionary.class)))
      .thenReturn(entityMock);
    var expectedEntityCount = PredicateDictionary.values().length - 1; //exclude PredicateDictionary.NULL

    //when
    dictionaryService.init();

    //then
    verify(predicateMapper, never())
      .toEntity(PredicateDictionary.NULL);
    verify(predicateMapper, times(expectedEntityCount))
      .toEntity(any());
    verify(predicateRepository)
      .saveAll(argThat(entities -> getCount(entities) == expectedEntityCount));
  }

  @Test
  void shouldInitTypes() {
    //given
    var entityMock = mock(ResourceTypeEntity.class);
    when(resourceTypeMapper.toEntity(any(ResourceTypeDictionary.class)))
      .thenReturn(entityMock);
    var expectedEntityCount = ResourceTypeDictionary.values().length;

    //when
    dictionaryService.init();

    //then
    verify(resourceTypeMapper, times(expectedEntityCount))
      .toEntity(any());
    verify(resourceTypeRepository)
      .saveAll(argThat(entities -> getCount(entities) == expectedEntityCount));
  }

  private long getCount(Iterable<?> list) {
    return StreamSupport.stream(list.spliterator(), false).count();
  }
}
