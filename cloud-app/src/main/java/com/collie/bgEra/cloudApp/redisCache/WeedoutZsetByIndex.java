package com.collie.bgEra.cloudApp.redisCache;

import java.lang.annotation.*;

/**
 * 向zset中添加项，并对zset进行修剪，按照正序或倒序保留执行的行数
 * keepRecordsSpEl表达式优先级高于keepRecords指定的值
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WeedoutZsetByIndex {
  String cacheKey();

  String addRecords();

  long keepRecords() default 10;

  String keepRecordsSpEl() default "";

  boolean reverse()default false;

}
