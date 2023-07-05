package org.folio.linked.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class LinkedDataApplication {


  public static void main(String[] args) {
    SpringApplication.run(LinkedDataApplication.class, args);

  }


}
