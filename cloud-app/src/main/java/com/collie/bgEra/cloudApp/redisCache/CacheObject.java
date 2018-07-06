package com.collie.bgEra.cloudApp.redisCache;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 根据cacheKey的值，从redis中获取对应的值，根据cacheKey的值可以是一个spel表达式。
 * 只能操作一个普通的redis key-value类型数据
 * 注解的具体实现{@link com.collie.bgEra.cloudApp.redisCache.RedisCacheAspect}
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheObject {

  /**
   * key的过期时间，和value互为别名
   * @return
   */
  @AliasFor("value")
  int expireTime() default 60;
  /**
   * key的过期时间，expireTime
   * @return
   */
  @AliasFor("expireTime")
  int value() default 60;

  String cacheKey() default "";

}
