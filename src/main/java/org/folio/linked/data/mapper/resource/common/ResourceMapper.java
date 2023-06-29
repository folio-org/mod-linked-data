package org.folio.linked.data.mapper.resource.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceMapper {

  /**
   * Resource Type, for example: lc:profile:bf2:Monograph.
   */
  String type() default "";

  /**
   * Resource Predicate, for example: http://id.loc.gov/ontologies/bibframe/contribution.
   */
  String predicate() default "";

  /**
   * Resource dto class, for example: org.folio.linked.data.domain.dto.WorkTitleInner.
   */
  Class<?> dtoClass() default Object.class;

}
