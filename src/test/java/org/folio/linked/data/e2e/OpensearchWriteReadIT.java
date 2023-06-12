package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opensearch.client.RequestOptions.DEFAULT;

import java.io.IOException;
import org.folio.linked.data.e2e.base.EnableElasticSearch;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles("opensearch")
@EnableElasticSearch
class OpensearchWriteReadIT {

  @Autowired
  private RestHighLevelClient elasticsearchClient;

  @Test
  void testWriteRead() throws IOException {
    // given
    var index = "something";
    var createRequest = new CreateIndexRequest(index);
    var getRequest = new GetIndexRequest(index);

    // when
    var createIndexResponse = elasticsearchClient.indices().create(createRequest, DEFAULT);
    var getIndexResponse = elasticsearchClient.indices().get(getRequest, DEFAULT);

    // then
    assertThat(createIndexResponse.index()).isEqualTo(index);
    assertThat(createIndexResponse.isAcknowledged()).isTrue();
    assertThat(getIndexResponse.getIndices()).contains(index);
  }

}
