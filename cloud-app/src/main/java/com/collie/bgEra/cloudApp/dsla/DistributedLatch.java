package com.collie.bgEra.cloudApp.dsla;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 为方法添加一个分布式锁，之后获取到锁，此方法才能被执行，否则会一直阻塞
 * 注解的具体实现{@link com.collie.bgEra.cloudApp.dsla.DistributedLatchAspect}
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLatch {
  @AliasFor("value")
  String latchName() default "defaultLatch";

  @AliasFor("latchName")
  String value() default "defaultLatch";
}
