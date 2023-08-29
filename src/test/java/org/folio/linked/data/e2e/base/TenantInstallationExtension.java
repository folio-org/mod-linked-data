package org.folio.linked.data.e2e.base;

import static java.util.Arrays.asList;
import static org.folio.linked.data.test.TestUtil.asJsonString;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.randomString;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@Log4j2
public class TenantInstallationExtension implements Extension, BeforeEachCallback {

  private static final String TENANT_ENDPOINT_URI = "/_/tenant";
  private static boolean init;

  @SneakyThrows
  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    if (!init) {
      var context = SpringExtension.getApplicationContext(extensionContext);
      var env = context.getEnvironment();
      if (asList(env.getActiveProfiles()).contains(FOLIO_PROFILE)) {
        var mockMvc = context.getBean(MockMvc.class);
        mockMvc.perform(post(TENANT_ENDPOINT_URI, randomString())
            .content(asJsonString(new TenantAttributes().moduleTo(env.getProperty("spring.application.name"))))
            .headers(defaultHeaders(env, extensionContext.getTestInstance().map(this::getOkapiUrl).orElse(null)))
            .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent());
      }
      init = true;
    }
  }

  private String getOkapiUrl(Object ti) {
    try {
      return ((OkapiConfiguration) ti.getClass().getField("okapi").get(null)).getOkapiUrl();
    } catch (NoSuchFieldException nsfe) {
      log.warn("Test instance contains no 'okapi' field. Add it if you want to mock the Okapi in your test");
    } catch (IllegalAccessException iae) {
      log.warn("Test instance contains 'okapi' field but it's not accessible");
    }
    return null;
  }

}
