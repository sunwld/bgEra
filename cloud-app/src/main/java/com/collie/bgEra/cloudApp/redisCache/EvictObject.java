package com.collie.bgEra.cloudApp.redisCache;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EvictObject {

  @AliasFor("value")
  String cacheKey() default "";
  @AliasFor("cacheKey")
  String value() default "";
}
