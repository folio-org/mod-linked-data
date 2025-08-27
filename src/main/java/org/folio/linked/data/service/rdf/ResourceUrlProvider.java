package org.folio.linked.data.service.rdf;

import java.util.function.LongFunction;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.integration.ConfigurationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceUrlProvider implements LongFunction<String> {

  private static final String URL_PATTERN = "%s/linked-data-editor/resources/%s";
  private final ConfigurationService configurationService;


  @Override
  public String apply(long id) {
    var folioHost = configurationService.getFolioHost();
    return String.format(URL_PATTERN, folioHost, id);
  }
}
