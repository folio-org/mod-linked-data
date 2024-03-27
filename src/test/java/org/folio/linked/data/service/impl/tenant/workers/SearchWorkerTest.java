package org.folio.linked.data.service.impl.tenant.workers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.client.SearchClient;
import org.folio.search.domain.dto.CreateIndexRequest;
import org.folio.spring.test.type.UnitTest;
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
    var expectedRequest = new CreateIndexRequest("bibframe");
    var attributes = mock(TenantAttributes.class);

    //when
    searchWorker.afterTenantUpdate(attributes);

    //then
    verify(searchClient)
      .createIndex(eq(expectedRequest));
  }
}
