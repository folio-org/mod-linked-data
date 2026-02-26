package org.folio.linked.data.configuration.batch;

import static org.folio.linked.data.configuration.batch.IndexableResourceQueryBuilder.buildParameterValues;
import static org.folio.linked.data.configuration.batch.IndexableResourceQueryBuilder.buildQueryProvider;

import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.support.AbstractItemStreamItemReader;

@Log4j2
public class ResourceReader extends AbstractItemStreamItemReader<Resource> {

  private final JdbcPagingItemReader<Resource> delegate;

  public ResourceReader(DataSource dataSource,
                        int chunkSize,
                        boolean isFullReindex,
                        String resourceType) {
    log.info("Initializing ResourceReader: isFullReindex={}, resourceType={}, chunkSize={}",
      isFullReindex, resourceType, chunkSize);
    this.delegate = new JdbcPagingItemReader<>(dataSource, buildQueryProvider(isFullReindex));
    this.delegate.setParameterValues(buildParameterValues(resourceType));
    this.delegate.setPageSize(chunkSize);
    this.delegate.setRowMapper(ResourceRowMapper.instance());
    this.delegate.setName("databaseResourceReader");
    try {
      this.delegate.afterPropertiesSet();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize JdbcPagingItemReader", e);
    }
  }


  @Override
  public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    delegate.open(executionContext);
  }

  @Override
  public Resource read() throws Exception {
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
