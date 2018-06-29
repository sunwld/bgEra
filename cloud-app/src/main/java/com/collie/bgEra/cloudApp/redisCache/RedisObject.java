package com.collie.bgEra.cloudApp.redisCache;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisObject {

  @AliasFor("value")
  int expireTime() default 60;

  @AliasFor("expireTime")
  int value() default 0;

}
