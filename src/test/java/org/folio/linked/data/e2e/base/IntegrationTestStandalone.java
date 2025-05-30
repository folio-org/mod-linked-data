package org.folio.linked.data.e2e.base;

import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.linked.data.LinkedDataApplication;
import org.folio.spring.testing.extension.EnablePostgres;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnablePostgres
@DirtiesContext
@AutoConfigureMockMvc
@org.folio.spring.testing.type.IntegrationTest
@SpringBootTest(classes = {LinkedDataApplication.class})
@ActiveProfiles({"test", STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
public @interface IntegrationTestStandalone {
}
