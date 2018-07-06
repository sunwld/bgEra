package com.collie.bgEra.cloudApp.redisCache;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 根据cacheKey，从redis中删除对应的数据
 * 注解的具体实现{@link com.collie.bgEra.cloudApp.redisCache.RedisCacheAspect}
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EvictObject {

  @AliasFor("value")
  String cacheKey() default "";
  @AliasFor("cacheKey")
  String value() default "";
}
