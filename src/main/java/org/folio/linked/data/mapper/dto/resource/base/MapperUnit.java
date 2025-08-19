package org.folio.linked.data.mapper.dto.resource.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MapperUnit {

  ResourceTypeDictionary type();

  PredicateDictionary[] predicate() default { PredicateDictionary.NULL };

  Class<?> requestDto() default Object.class;

}
