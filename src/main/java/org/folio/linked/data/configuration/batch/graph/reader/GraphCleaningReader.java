package org.folio.linked.data.configuration.batch.graph.reader;

import static org.folio.linked.data.util.JdbcUtil.COL_RESOURCE_HASH;

import javax.sql.DataSource;
import lombok.NonNull;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.support.AbstractItemStreamItemReader;
import org.springframework.jdbc.core.RowMapper;

public class GraphCleaningReader extends AbstractItemStreamItemReader<Long> {

  private final JdbcPagingItemReader<Long> delegate;

  public GraphCleaningReader(DataSource dataSource, int chunkSize) {
    this.delegate = new JdbcPagingItemReader<>(dataSource, GraphCleaningQueryProvider.build());
    this.delegate.setPageSize(chunkSize);
    this.delegate.setRowMapper(rowMapper());
    this.delegate.setName(this.getClass().getSimpleName());
    try {
      this.delegate.afterPropertiesSet();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize JdbcPagingItemReader", e);
    }
  }

  private RowMapper<Long> rowMapper() {
    return (rs, rowNum) -> rs.getLong(COL_RESOURCE_HASH);
  }

  @Override
  public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    delegate.open(executionContext);
  }

  @Override
  public Long read() throws Exception {
    return delegate.read();
  }

  @Override
  public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    delegate.update(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    delegate.close();
  }

}
