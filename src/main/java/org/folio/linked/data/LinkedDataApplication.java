package org.folio.linked.data;

import static org.springframework.context.annotation.ComponentScan.Filter;
import static org.springframework.context.annotation.FilterType.REGEX;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableAsync
@EnableScheduling
@EnableResilientMethods
@SpringBootApplication
@ComponentScan(value = "org.folio", excludeFilters = @Filter(type = REGEX, pattern =
  {"org.folio.spring.tools.systemuser.*", "org.folio.spring.tools.batch.*"}))
public class LinkedDataApplication {


  public static void main(String[] args) {
    SpringApplication.run(LinkedDataApplication.class, args);
  }

}
