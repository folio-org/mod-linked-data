package org.folio.linked.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableResilientMethods
@SpringBootApplication
@ComponentScan(value = "org.folio")
public class LinkedDataApplication {

  public static void main(String[] args) {
    SpringApplication.run(LinkedDataApplication.class, args);
  }

}
