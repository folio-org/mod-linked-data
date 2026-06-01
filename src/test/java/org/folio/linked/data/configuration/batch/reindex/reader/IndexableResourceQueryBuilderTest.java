package org.folio.linked.data.configuration.batch.reindex.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.configuration.batch.reindex.reader.IndexableResourceQueryBuilder.buildParameterValues;
import static org.folio.linked.data.configuration.batch.reindex.reader.IndexableResourceQueryBuilder.buildQueryProvider;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
@SuppressWarnings("unchecked")
class IndexableResourceQueryBuilderTest {

  @Test
  @SneakyThrows
  void buildQueryProvider_shouldExcludeLightResources_inFullReindexMode() {
    // given
    var provider = buildQueryProvider(true);
    provider.init(mock(DataSource.class));

    // when
    var sql = provider.generateFirstPageQuery(10);

    // then
    assertThat(sql)
      .contains("NOT EXISTS")
      .contains(String.valueOf(LIGHT_RESOURCE.getHash()));
  }

  @Test
  @SneakyThrows
  void buildQueryProvider_shouldExcludeLightResources_inIncrementalReindexMode() {
    // given
    var provider = buildQueryProvider(false);
    provider.init(mock(DataSource.class));

    // when
    var sql = provider.generateFirstPageQuery(10);

    // then
    assertThat(sql)
      .contains("index_date IS NULL")
      .contains("NOT EXISTS")
      .contains(String.valueOf(LIGHT_RESOURCE.getHash()));
  }

  @Test
  void buildParameterValues_shouldReturnAllSupportedTypeHashes_whenResourceTypeIsBlank() {
    // when
    var params = buildParameterValues(null);

    // then
    assertThat(params).containsOnlyKeys("typeHashes");
    assertThat((Iterable<Long>) params.get("typeHashes"))
      .containsExactlyInAnyOrder(WORK.getHash(), HUB.getHash());
  }

  @Test
  void buildParameterValues_shouldReturnSingleTypeHash_whenResourceTypeIsProvided() {
    // when
    var params = buildParameterValues("work");

    // then
    assertThat((Iterable<Long>) params.get("typeHashes"))
      .containsExactly(WORK.getHash());
  }
}




