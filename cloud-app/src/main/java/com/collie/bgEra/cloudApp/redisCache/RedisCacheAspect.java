package com.collie.bgEra.cloudApp.redisCache;

import com.collie.bgEra.cloudApp.redisCache.RedisService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisCacheAspect {

  @Autowired
  private RedisService redisUtil = null;

  @Pointcut("@annotation(rs)")
  public  void annotationPointCut(RedisObject rs) {}

  @Around(value = "annotationPointCut(rs))")
  public Object redisCacheCommon(ProceedingJoinPoint pjp,RedisObject rs) throws Throwable {
    try {
      Object[] args = pjp.getArgs();
      Object key = args[0];
      if(key == null){
        return pjp.proceed();
      }

      //从redis中获取数据
      Object result = redisUtil.getObject(key);
      //如果redis中不存在该数据，则从方法中获取，并缓存到redis中
      if(result == null){
        result = pjp.proceed();
        System.out.println(rs.expireTime());
        redisUtil.setObject(key, result, rs.expireTime());
      }
      return result;
    } catch (Throwable throwable) {
      throw throwable;
    }
  }
}
