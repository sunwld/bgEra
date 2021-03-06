package com.collie.bgEra.cloudApp.redisCache;

import java.lang.annotation.*;

/**
 * 从zset中取出并删除index区间（闭合区间）内的数据,默认为正序
 * minIndexSpEl和maxIndexSpEl 表达式指定的值优先级高于minIndex和maxIndex 指定的值
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZsetPopByIndex {
  String cacheKey();

  long minIndex() default Integer.MIN_VALUE;

  long maxIndex()default Integer.MAX_VALUE;

  String minIndexSpEl() default "";

  String maxIndexSpEl() default "";

  boolean reverse() default false;

}
