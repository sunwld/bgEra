package com.collie.bgEra.cloudApp.dsla;

import com.collie.bgEra.cloudApp.redisCache.CacheObject;
import com.collie.bgEra.cloudApp.redisCache.EvictObject;
import com.collie.bgEra.cloudApp.redisCache.RedisService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DistributedLatchAspect {
  @Pointcut("@annotation(dl)")
  public  void distributedLatchPointCut(DistributedLatch dl) {}

  @Around(value = "distributedLatchPointCut(dl))")
  public Object redisCacheCommon(ProceedingJoinPoint pjp,DistributedLatch dl) throws Throwable {
    String latchId = null;
    String latchName = null;
    try {
      //为了使注解中的@AliasFor生效,需要使用AnnotationUtils工具类，对自定义注解进行代理
      dl = AnnotationUtils.getAnnotation(dl,dl.getClass());
      latchName = dl.value();
      latchId = DistributedServiceLatchArbitrator.apply().grabLatch(latchName);
      return pjp.proceed();
    } finally {
      DistributedServiceLatchArbitrator.apply().releaseLatch(latchName,latchId);
    }
  }
}
