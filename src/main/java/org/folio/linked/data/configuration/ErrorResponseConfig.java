package org.folio.linked.data.configuration;

import java.util.List;
import lombok.Data;
import org.folio.ld.fingerprint.config.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:errors.yml", factory = YamlPropertySourceFactory.class)
public class ErrorResponseConfig {

  private ErrorResponseConfig.Error alreadyExistsException;
  private ErrorResponseConfig.Error notFoundException;

  public record Error(int status, String code, List<String> parameters, String message) {
  }

}
