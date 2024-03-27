package org.folio.linked.data.configuration.standalone;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!" + FOLIO_PROFILE)
public class StandaloneExcludeProcessor implements BeanDefinitionRegistryPostProcessor {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    getExcludeBeans()
      .forEach(bean -> removeFromContext(bean, registry));
  }

  private Collection<String> getExcludeBeans() {
    return List.of(
      "defaultTenantController",
      "defaultTenantOkapiHeaderValidationFilter"
    );
  }

  private void removeFromContext(String beanName, BeanDefinitionRegistry registry) {
    if (registry.containsBeanDefinition(beanName)) {
      registry.removeBeanDefinition(beanName);
    }
  }
}
