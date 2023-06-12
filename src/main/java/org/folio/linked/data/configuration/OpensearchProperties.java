package org.folio.linked.data.configuration;

import java.time.Duration;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("spring.opensearch")
@Profile("opensearch")
public class OpensearchProperties {

  private List<String> uris;

  private String username;

  private String password;

  private Duration connectionTimeout = Duration.ofSeconds(1);

  private Duration socketTimeout = Duration.ofSeconds(30);

  private String pathPrefix;

  private boolean compressionEnabled;

}
