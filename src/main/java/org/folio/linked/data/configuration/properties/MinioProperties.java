package org.folio.linked.data.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mod-linked-data.minio")
@Data
public class MinioProperties {

  private String endpoint;
  private String accessKey;
  private String secretKey;
}
