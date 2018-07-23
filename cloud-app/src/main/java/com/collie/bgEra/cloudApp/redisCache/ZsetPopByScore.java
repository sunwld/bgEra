package com.collie.bgEra.cloudApp.redisCache;

import java.lang.annotation.*;

/**
 * 从zset中取出并删除score区间（闭合区间）内的数据,
 * minScoreSpEl和maxScoreSpEl spel表达式的值优先级高于minScore和maxScore指定的值
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZsetPopByScore {
  String cacheKey();

  double minScore() default 0-Double.MAX_VALUE;

  double maxScore() default Double.MAX_VALUE;

  String minScoreSpEl() default "";

  String maxScoreSpEl() default "";

}
