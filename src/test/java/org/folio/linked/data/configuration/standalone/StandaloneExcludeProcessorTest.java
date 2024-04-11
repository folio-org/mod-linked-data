package org.folio.linked.data.configuration.standalone;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

@UnitTest
class StandaloneExcludeProcessorTest {

  private final StandaloneExcludeProcessor processor = new StandaloneExcludeProcessor();

  @ParameterizedTest
  @ValueSource(strings = {
    "defaultTenantController",
    "defaultTenantOkapiHeaderValidationFilter"
  })
  void shouldExcludeBeans(String beanName) {
    //given
    var registry = mock(BeanDefinitionRegistry.class);
    when(registry.containsBeanDefinition(beanName))
      .thenReturn(true);

    //when
    processor.postProcessBeanDefinitionRegistry(registry);

    //then
    verify(registry)
      .removeBeanDefinition(beanName);
  }
}
