package org.folio.linked.data.service.tenant.worker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.client.SearchClient;
import org.folio.search.domain.dto.CreateIndexRequest;
import org.folio.spring.testing.type.UnitTest;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SearchWorkerTest {

  @Mock
  private SearchClient searchClient;

  @InjectMocks
  private SearchWorker searchWorker;

  @Test
  void shouldInitSearchIndex() {
    //given
    var expectedRequest = new CreateIndexRequest("linked-data-work");
    var attributes = mock(TenantAttributes.class);
    var tenantId = "tenant-01";

    //when
    searchWorker.afterTenantUpdate(tenantId, attributes);

    //then
    verify(searchClient).createIndex(expectedRequest);
  }

}
