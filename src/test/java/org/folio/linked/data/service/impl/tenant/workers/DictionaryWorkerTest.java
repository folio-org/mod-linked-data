package org.folio.linked.data.service.impl.tenant.workers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.service.DictionaryService;
import org.folio.spring.test.type.UnitTest;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DictionaryWorkerTest {

  @Mock
  private DictionaryService dictionaryService;

  @InjectMocks
  private DictionaryWorker dictionaryWorker;

  @Test
  void shouldInitDictionaries() {
    //given
    var attributes = mock(TenantAttributes.class);

    //when
    dictionaryWorker.afterTenantUpdate(attributes);

    //then
    verify(dictionaryService)
      .init();
  }
}
