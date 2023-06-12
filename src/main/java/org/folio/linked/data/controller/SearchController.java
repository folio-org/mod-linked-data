package org.folio.linked.data.controller;

import static org.opensearch.client.RequestOptions.DEFAULT;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.rest.resource.SearchApi;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("opensearch")
public class SearchController implements SearchApi {

  @Autowired
  private final RestHighLevelClient elasticsearchClient;

  @SneakyThrows
  @Override
  public ResponseEntity<String> createIndex(String index) {
    var createIndexRequest = new CreateIndexRequest(index);
    var response = elasticsearchClient.indices().create(createIndexRequest, DEFAULT);
    return ResponseEntity.ok(response.index());
  }

  @SneakyThrows
  @Override
  public ResponseEntity<List<String>> getIndices(String index) {
    var getRequest = new GetIndexRequest(index);
    var response = elasticsearchClient.indices().get(getRequest, DEFAULT);
    return ResponseEntity.ok(Arrays.asList(response.getIndices()));
  }
}
