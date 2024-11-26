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

  private ErrorResponseConfig.Error alreadyExists;
  private ErrorResponseConfig.Error mapping;
  private ErrorResponseConfig.Error notFound;
  private ErrorResponseConfig.Error notSupported;
  private ErrorResponseConfig.Error required;
  private ErrorResponseConfig.Error genericBadRequest;
  private ErrorResponseConfig.Error validation;
  private ErrorResponseConfig.Error genericServer;
  private ErrorResponseConfig.Error failedDependency;

  public record Error(int status, String code, List<String> parameters, String message) {
  }

}
