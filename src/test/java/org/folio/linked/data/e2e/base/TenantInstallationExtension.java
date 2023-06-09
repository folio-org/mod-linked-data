package org.folio.linked.data.e2e.base;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.randomString;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

public class TenantInstallationExtension implements Extension, BeforeEachCallback, AfterAllCallback {

  private static final String TENANT_ENDPOINT_URL = "/_/tenant";
  private MockMvc mockMvc;
  private String appName;
  private Environment env;

  @SneakyThrows
  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    if (isNull(appName)) {
      var context = SpringExtension.getApplicationContext(extensionContext);
      env = context.getEnvironment();
      appName = env.getProperty("spring.application.name");
      if (asList(env.getActiveProfiles()).contains(FOLIO_PROFILE)) {
        mockMvc = context.getBean(MockMvc.class);
        mockMvc.perform(post(TENANT_ENDPOINT_URL, randomString())
            .content(asJsonString(new TenantAttributes().moduleTo(appName)))
            .headers(defaultHeaders(env))
            .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent());
      }
    }
  }

  @SneakyThrows
  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (asList(env.getActiveProfiles()).contains(FOLIO_PROFILE)) {
      mockMvc.perform(post(TENANT_ENDPOINT_URL, randomString())
          .content(asJsonString(new TenantAttributes().moduleFrom(appName).purge(false)))
          .headers(defaultHeaders(env)))
        .andExpect(status().isNoContent());
    }
  }

}
