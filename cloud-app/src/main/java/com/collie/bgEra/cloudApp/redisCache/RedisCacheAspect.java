package com.collie.bgEra.cloudApp.redisCache;

import com.collie.bgEra.cloudApp.dsla.DistributedServiceLatchArbitrator;
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean;
import com.collie.bgEra.commons.util.AspectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Aspect
@Component
public class RedisCacheAspect {
  private static Logger logger = LoggerFactory.getLogger("redisCache");

  @Autowired
  private RedisService redisService = null;

//  @Pointcut("@annotation(co)")
//  public  void cacheObjectPointCut(CacheObject co) {}
//
//  @Pointcut("@annotation(eo)")
//  public  void evictObjectPointCut(EvictObject eo) {}
//
//  @Pointcut("@annotation(popZsetByScoreAnno)")
//  public  void popZsetByScorePointCut(PopZsetByScore popZsetByScoreAnno) {}
//
//  @Pointcut("@annotation(weedoutZsetByIndex)")
//  public  void weedoutZsetByIndexPointCut(WeedoutZsetByIndex weedoutZsetByIndex) {}



  @Around(value = "@annotation(co))")
  public Object redisCacheCommon(ProceedingJoinPoint pjp,CacheObject co) throws Throwable {
    Object result = null;
    String latchId = null;
    String key = null;
    try {
      //为了使注解中的@AliasFor生效,需要使用AnnotationUtils工具类，对自定义注解进行代理
      co = AnnotationUtils.getAnnotation(co,co.getClass());

      Object[] args = pjp.getArgs();
      key = AspectUtils.parseKey(co.cacheKey(),AspectUtils.getMethod(pjp),args);
      if(key == null){
        logger.debug("key : " + co.cacheKey() + "is Null, query data from db");
        return pjp.proceed();
      }

      //从redis中获取数据
      result = redisService.getObject(key);
      //如果redis中不存在该数据，则从方法中获取，并缓存到redis中
      if(result == null){
        latchId = getLatch(key);
        logger.debug("key : " + co.cacheKey() + " does not exist in redis, query data from db");
        result = pjp.proceed();
        redisService.setObject(key, result, co.expireTime());
      }else{
        logger.debug("key : " + co.cacheKey() + "is exist in redis, query data from redis");
      }
      return result;
    } finally {
      releaseLatch(key,latchId);
    }
  }

  @Around("@annotation(eo)")
  public Object updataCacheCommon(ProceedingJoinPoint pjp,EvictObject eo) throws Throwable {
    String latchId = null;
    String key = null;
    try {
      eo = AnnotationUtils.getAnnotation(eo,eo.getClass());
      Object[] args = pjp.getArgs();
      key = AspectUtils.parseKey(eo.cacheKey(),AspectUtils.getMethod(pjp),args);
      if(key == null){
        throw new IllegalArgumentException("cacheKey cannot be null ");
      }
      latchId = getLatch(key);
      redisService.delKey(key);
      return pjp.proceed();
    } finally {
      releaseLatch(key,latchId);
    }
  }

  @Around("@annotation(anno)")
  public List<ZSetItemBean> popZsetByScoreCommon(ProceedingJoinPoint pjp,PopZsetByScore anno) throws Throwable {
    String latchId = null;
    String key = null;
    List<ZSetItemBean> result = null;
    Double min = 0D;
    Double max = 0D;
    try {
      Object[] args = pjp.getArgs();
      anno = AnnotationUtils.getAnnotation(anno,anno.getClass());
      key = AspectUtils.parseKey(anno.cacheKey(),AspectUtils.getMethod(pjp),args);
      if(key == null){
        throw new IllegalArgumentException("cacheKey cannot be null ");
      }

      min = anno.minScore();
      max = anno.maxScore();

      //如果没有为minScore或者maxScore指定 spel表达式，则直接取minScore和maxScore的值
      //如果指定了，则以spel表达式的值覆盖minScore和maxScore的值
      if(!"".equals(anno.minScoreSpEl())){
        min = AspectUtils.parseKey(anno.minScoreSpEl(),AspectUtils.getMethod(pjp),args,Double.class);
      }
      if(!"".equals(anno.maxScoreSpEl())){
        max = AspectUtils.parseKey(anno.maxScoreSpEl(),AspectUtils.getMethod(pjp),args,Double.class);
      }

      if(redisService.hasKey(key)){
        result = redisService.popZSetItemByScoreWithScore(key,min,max);
      }else{
        latchId = getLatch(key);
        result = (List<ZSetItemBean>) pjp.proceed();
        redisService.addZsetItems(key,result, -1);
        result = redisService.popZSetItemByScoreWithScore(key,min,max);
      }
      return result;
    } finally {
      releaseLatch(key,latchId);
    }
  }

  @Around("@annotation(anno)")
  public Object popZsetByScoreCommon(ProceedingJoinPoint pjp,WeedoutZsetByIndex anno) throws Throwable {
    String latchId = null;
    String key = null;
    boolean reverse;
    long keepRecords;
    List<ZSetItemBean> addRecords = new ArrayList<>();
    try {
      anno = AnnotationUtils.getAnnotation(anno,anno.getClass());
      Object[] args = pjp.getArgs();
      key = AspectUtils.parseKey(anno.cacheKey(),AspectUtils.getMethod(pjp),args);
      if(key == null){
        throw new IllegalArgumentException("cacheKey cannot be null ");
      }

      reverse = anno.reverse();
      keepRecords = anno.keepRecords();
      //如果指定了keepRecordsSpEl，则解析此表达式的值，并覆盖keepRecords指定的值
      if(!"".equals(anno.keepRecordsSpEl())){
        keepRecords = AspectUtils.parseKey(anno.keepRecordsSpEl(),AspectUtils.getMethod(pjp),args,Long.class);
      }
      addRecords = AspectUtils.parseKey(anno.addRecords(),AspectUtils.getMethod(pjp),args,addRecords.getClass());

      latchId = getLatch(key);
      redisService.addZsetItemsAndTrimByIndex(key,addRecords,keepRecords,-1,reverse);
      return pjp.proceed();
    } finally {
      releaseLatch(key,latchId);
    }
  }

  @Around("@annotation(anno)")
  public Object putHsetCommon(ProceedingJoinPoint pjp,PutHset anno) throws Throwable {
    String key = null;
    Map<String,Object> putMap = new HashMap<>();
    anno = AnnotationUtils.getAnnotation(anno,anno.getClass());
    Object[] args = pjp.getArgs();
    key = AspectUtils.parseKey(anno.cacheKey(),AspectUtils.getMethod(pjp),args);
    if(key == null){
      throw new IllegalArgumentException("cacheKey cannot be null ");
    }

    putMap = AspectUtils.parseKey(anno.map(),AspectUtils.getMethod(pjp),args,putMap.getClass());
    redisService.hsetPut(key,putMap);
    return pjp.proceed();
  }

  @Around("@annotation(anno)")
  public Object getHsetItemCommon(ProceedingJoinPoint pjp,GetHsetItem anno) throws Throwable {
    String key = null;
    String field = null;
    Object result = null;
    anno = AnnotationUtils.getAnnotation(anno,anno.getClass());
    Object[] args = pjp.getArgs();
    key = AspectUtils.parseKey(anno.cacheKey(),AspectUtils.getMethod(pjp),args);
    if(key == null){
      throw new IllegalArgumentException("cacheKey cannot be null ");
    }

    field = AspectUtils.parseKey(anno.field(),AspectUtils.getMethod(pjp),args);
    result = redisService.hsetGet(key,field);
    if(result == null){
      result = pjp.proceed();
      redisService.hsetPut(key,field,result);
    }
    return result;
  }

  @Around("@annotation(anno)")
  public Object delHsetItemCommon(ProceedingJoinPoint pjp,DelHsetItem anno) throws Throwable {
    String key = null;
    String field = null;
    anno = AnnotationUtils.getAnnotation(anno,anno.getClass());
    Object[] args = pjp.getArgs();
    key = AspectUtils.parseKey(anno.cacheKey(),AspectUtils.getMethod(pjp),args);
    if(key == null){
      throw new IllegalArgumentException("cacheKey cannot be null ");
    }
    field = AspectUtils.parseKey(anno.field(),AspectUtils.getMethod(pjp),args);
    redisService.hsetDelItem(key,field);
    return pjp.proceed();
  }

  private String getLatch(String key){
    return DistributedServiceLatchArbitrator.apply().grabLatch(key);
  }

  private void releaseLatch(String key,String latchId){
    if(latchId != null){
      DistributedServiceLatchArbitrator.apply().releaseLatch(key,latchId);
    }
  }
}
