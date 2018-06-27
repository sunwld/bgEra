package com.collie.bgEra.cloudApp.redisCache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisObject {

  int expireTime() default 60;

}
