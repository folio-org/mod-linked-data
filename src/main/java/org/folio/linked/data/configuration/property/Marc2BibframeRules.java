package org.folio.linked.data.configuration.property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:marc2bibframe.yml", factory = YamlPropertySourceFactory.class)
public class Marc2BibframeRules {

  private Map<String, List<FieldRule>> fieldRules;

  @Data
  public static class FieldRule {
    private Set<String> types = new HashSet<>();
    private String parent;
    private String predicate;
    private FieldCondition condition;
    private Map<Character, String> subfields = new HashMap<>();
    private String ind1;
    private String ind2;
    private Character labelField;
  }

  @Data
  public static class FieldCondition {
    private Map<Character, String> fields = new HashMap<>();
    private String ind1;
    private String ind2;
  }
}
