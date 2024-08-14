package org.folio.linked.data.service.tenant.worker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.service.DictionaryService;
import org.folio.spring.testing.type.UnitTest;
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
    var tenantId = "tenant-01";

    //when
    dictionaryWorker.afterTenantUpdate(tenantId, attributes);

    //then
    verify(dictionaryService)
      .init();
  }
}
