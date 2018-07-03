package com.collie.bgEra.cloudApp.redisCache;

import com.collie.bgEra.cloudApp.appm.DistributedServiceLatchArbitrator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
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
public class RedisCacheAspect {
  @Autowired
  private RedisService redisUtil = null;

  @Pointcut("@annotation(co)")
  public  void cacheObjectPointCut(CacheObject co) {}

  @Pointcut("@annotation(eo)")
  public  void evictObjectPointCut(EvictObject eo) {}

  @Around(value = "cacheObjectPointCut(co))")
  public Object redisCacheCommon(ProceedingJoinPoint pjp,CacheObject co) throws Throwable {
    Object result = null;
    String latchId = null;
    String key = null;
    try {
      Object[] args = pjp.getArgs();
      key = parseKey(co.cacheKey(),getMethod(pjp),args);
      if(key == null){
        return pjp.proceed();
      }
      //为了使注解中的@AliasFor生效,需要使用AnnotationUtils工具类，对自定义注解进行代理
      co = AnnotationUtils.getAnnotation(co,co.getClass());
      //从redis中获取数据
      result = redisUtil.getObject(key);
      //如果redis中不存在该数据，则从方法中获取，并缓存到redis中
      if(result == null){
        latchId = DistributedServiceLatchArbitrator.apply().grabLatch(key);
        result = pjp.proceed();
        redisUtil.setObject(key, result, co.expireTime());
      }
      return result;
    } finally {
      DistributedServiceLatchArbitrator.apply().releaseLatch(key,latchId);
    }
  }

  @Around("evictObjectPointCut(eo)")
  public Object updataCacheCommon(ProceedingJoinPoint pjp,EvictObject eo) throws Throwable {
    String latchId = null;
    String key = null;
    try {
      eo = AnnotationUtils.getAnnotation(eo,eo.getClass());
      Object[] args = pjp.getArgs();
      key = parseKey(eo.cacheKey(),getMethod(pjp),args);
      if(key == null){
        throw new IllegalArgumentException("cacheKey cannot be null ");
      }
      latchId = DistributedServiceLatchArbitrator.apply().grabLatch(key);
      redisUtil.del(key);
      return pjp.proceed();
    } finally {
      DistributedServiceLatchArbitrator.apply().releaseLatch(key,latchId);
    }
  }

  private Method getMethod(JoinPoint jp){
    MethodSignature methodSignature = (MethodSignature)jp.getSignature();
    return methodSignature.getMethod();
  }

  //用于获取Mthod的参数名列表
  private LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
  //使用SPEL进行解析
  private ExpressionParser parser = new SpelExpressionParser();
  private String parseKey(String spEL, Method method, Object[] args){
    //获取被拦截方法参数名列表(使用Spring支持类库)
    String [] paraNameArr=u.getParameterNames(method);
    //SPEL上下文
    StandardEvaluationContext context = new StandardEvaluationContext();
    //把方法参数放入SPEL上下文中
    for(int i=0;i<paraNameArr.length;i++){
      context.setVariable(paraNameArr[i], args[i]);
    }
    return parser.parseExpression(spEL).getValue(context,String.class);
  }
}
