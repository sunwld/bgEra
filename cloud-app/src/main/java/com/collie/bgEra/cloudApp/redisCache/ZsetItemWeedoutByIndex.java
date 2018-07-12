package com.collie.bgEra.cloudApp.redisCache;

import java.lang.annotation.*;

/**
 * 向zset中添加一项，并对zset进行修剪，按照正序或倒序保留执行的行数
 * keepRecordsSpEl表达式优先级高于keepRecords指定的值
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZsetItemWeedoutByIndex {
  String cacheKey();

  String itemIdEl();

  double itemScore() default 0d;

  String itemScoreEl() default "";

  long keepRecords() default -1;

  String keepRecordsSpEl() default "";

  boolean reverse()default false;

}
