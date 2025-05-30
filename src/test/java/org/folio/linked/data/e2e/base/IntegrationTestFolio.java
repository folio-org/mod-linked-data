package org.folio.linked.data.e2e.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.LinkedDataApplication;
import org.folio.linked.data.configuration.kafka.LinkedDataTopicProperties;
import org.folio.spring.testing.extension.EnableKafka;
import org.folio.spring.testing.extension.EnableOkapi;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.spring.tools.kafka.FolioKafkaProperties;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableOkapi
@EnableKafka
@EnablePostgres
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.folio.spring.testing.type.IntegrationTest
@ExtendWith(TenantInstallationExtension.class)
@SpringBootTest(classes = {KafkaAdminService.class, KafkaAutoConfiguration.class,
  FolioKafkaProperties.class, LinkedDataTopicProperties.class, LinkedDataApplication.class})
public @interface IntegrationTestFolio {
}
