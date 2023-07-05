package org.folio.linked.data.configuration.properties;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mod-linked-data.bibframe")
public class BibframeProperties {

  private Set<String> profiles = new HashSet<>();
}
