package org.folio.linked.data.configuration.batch;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.folio.linked.data.configuration.batch.BatchConfig.SUPPORTED_TYPES;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.support.AbstractItemStreamItemReader;

@Log4j2
public class ResourceReader extends AbstractItemStreamItemReader<Resource> {

  private static final String TYPE_URIS = "typeURIs";
  private static final String SELECTION_QUERY_FULL =
    "SELECT DISTINCT r FROM Resource r JOIN r.types t WHERE t.uri in (:" + TYPE_URIS + ")";
  private static final String SELECTION_QUERY_NOT_INDEXED =
    "SELECT DISTINCT r FROM Resource r JOIN r.types t WHERE r.indexDate IS NULL AND t.uri in (:" + TYPE_URIS + ")";
  private final JpaPagingItemReader<Resource> delegate;

  public ResourceReader(EntityManagerFactory entityManagerFactory,
                        int chunkSize,
                        boolean isFullReindex,
                        String resourceType) {
    log.info("Initializing ResourceReader: isFullReindex={}, resourceType={}, chunkSize={}",
      isFullReindex, resourceType, chunkSize);
    this.delegate = new JpaPagingItemReader<>(entityManagerFactory);
    this.delegate.setQueryString(getQueryString(isFullReindex));
    this.delegate.setParameterValues(Map.of(TYPE_URIS, getTypeUris(resourceType)));
    this.delegate.setPageSize(chunkSize);
    this.delegate.setName("databaseResourceReader");
  }

  private String getQueryString(boolean isFullReindex) {
    return isFullReindex ? SELECTION_QUERY_FULL : SELECTION_QUERY_NOT_INDEXED;
  }

  private List<String> getTypeUris(String resourceType) {
    if (isBlank(resourceType)) {
      return SUPPORTED_TYPES.stream()
        .map(ResourceTypeDictionary::getUri)
        .toList();
    }
    return List.of(ResourceTypeDictionary.valueOf(resourceType.toUpperCase()).getUri());
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
