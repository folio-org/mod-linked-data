package org.folio.linked.data.e2e.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.spring.test.extension.EnableOkapi;
import org.folio.spring.test.extension.EnablePostgres;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@org.folio.spring.test.type.IntegrationTest
@SpringBootTest
@EnableOkapi
@EnablePostgres
@AutoConfigureMockMvc
@TestPropertySource(properties = {"env = folio-test"})
public @interface IntegrationTest {
}
