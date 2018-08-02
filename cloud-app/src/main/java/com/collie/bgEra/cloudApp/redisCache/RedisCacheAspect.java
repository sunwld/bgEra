package com.collie.bgEra.cloudApp.redisCache;

import com.collie.bgEra.cloudApp.dsla.DistributedServiceLatchArbitrator;
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean;
import com.collie.bgEra.commons.util.AspectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.*;


@Aspect
@Component
public class RedisCacheAspect {
    private static Logger logger = LoggerFactory.getLogger("redisCache");

    @Autowired
    private RedisService redisService = null;

    @Autowired
    @Qualifier("distributedServiceLatchArbitrator")
    private DistributedServiceLatchArbitrator latchArbitrator = null;

    @Around(value = "@annotation(co))")
    public Object redisCacheCommon(ProceedingJoinPoint pjp, CacheObject co) throws Throwable {
        Object result;
        String latchId = null;
        String key = null;
        try {
            //为了使注解中的@AliasFor生效,需要使用AnnotationUtils工具类，对自定义注解进行代理
            co = AnnotationUtils.getAnnotation(co, co.getClass());

            Object[] args = pjp.getArgs();
            Method jpMethod = AspectUtils.getMethod(pjp);
            key = AspectUtils.parseKey(co.cacheKey(), jpMethod, args);
            if (key == null) {
                logger.debug("key : " + co.cacheKey() + "is Null, query data from db");
                return pjp.proceed();
            }

            //从redis中获取数据
            result = redisService.getObject(key);
            //如果redis中不存在该数据，则从方法中获取，并缓存到redis中
            if (result == null) {
                latchId = getLatch(key);
                logger.debug("key : " + co.cacheKey() + " does not exist in redis, query data from db");
                result = pjp.proceed();
                logger.debug("run jpMethod: " + jpMethod.getName() + "by @CacheObject");
                redisService.setObject(key, result, co.expireTime());
            } else {
                logger.debug("key : " + co.cacheKey() + "is exist in redis, query data from redis");
            }
            return result;
        } finally {
            releaseLatch(key, latchId);
        }
    }

    @Around("@annotation(eo)")
    public Object updataCacheCommon(ProceedingJoinPoint pjp, EvictObject eo) throws Throwable {
        String latchId = null;
        String key = null;
        Object result;
        try {
            eo = AnnotationUtils.getAnnotation(eo, eo.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, eo.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();

            latchId = getLatch(key);
            redisService.delKey(key);
            logger.debug("delete key: " + key + "from redis");
            result = pjp.proceed();
            logger.debug("run jpMethod: " + jpMethod.getName() + "by @EvictObject");
            return result;
        } finally {
            releaseLatch(key, latchId);
        }
    }

    @Around("@annotation(anno)")
    public List<ZSetItemBean> popZsetByScoreCommon(ProceedingJoinPoint pjp, ZsetPopByScore anno) throws Throwable {
        String latchId = null;
        String key = null;
        List<ZSetItemBean> result = null;
        Double min = 0d;
        Double max = 0d;
        try {
            anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();

            min = anno.minScore();
            max = anno.maxScore();

            //如果没有为minScore或者maxScore指定 spel表达式，则直接取minScore和maxScore的值
            //如果指定了，则以spel表达式的值覆盖minScore和maxScore的值
            if (!"".equals(anno.minScoreSpEl())) {
                min = AspectUtils.parseKey(anno.minScoreSpEl(), jpMethod, args, Double.class);
            }
            if (!"".equals(anno.maxScoreSpEl())) {
                max = AspectUtils.parseKey(anno.maxScoreSpEl(), jpMethod, args, Double.class);
            }

            if (redisService.hasKey(key)) {
                result = redisService.popZSetItemByScoreWithScore(key, min, max);
                logger.trace("pop zset item from redis, redis key:" + key + ", minScore:" + min + ",maxScore" + max + ", items:" + result);
            } else {
                latchId = getLatch(key);
                result = (List<ZSetItemBean>) pjp.proceed();
                redisService.addZsetItems(key, result, -1);
                result = redisService.popZSetItemByScoreWithScore(key, min, max);
                logger.trace("pop zset item from db, redis key:" + key + ", minScore:" + min + ",maxScore" + max + ", items:" + result);
            }
            return result;
        } finally {
            releaseLatch(key, latchId);
        }
    }

    @Around("@annotation(anno)")
    public Object weedoutZsetByIndexCommon(ProceedingJoinPoint pjp, ZsetWeedoutByIndex anno) throws Throwable {
        String latchId = null;
        String key = null;
        Object result;
        boolean reverse;
        long keepRecords;
        List<ZSetItemBean> addRecords = new ArrayList<>();
        try {
            anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();

            reverse = anno.reverse();
            keepRecords = anno.keepRecords();
            //如果指定了keepRecordsSpEl，则解析此表达式的值，并覆盖keepRecords指定的值
            if (!"".equals(anno.keepRecordsSpEl())) {
                keepRecords = AspectUtils.parseKey(anno.keepRecordsSpEl(), jpMethod, args, Long.class);
            }
            addRecords = AspectUtils.parseKey(anno.addRecords(), jpMethod, args, addRecords.getClass());

            logger.trace("add zset item to redis, redis key:" + key + "keep the zset length to " + keepRecords);
            result = pjp.proceed();
            logger.trace("run jpMethod: " + jpMethod.getName() + "by @ZsetWeedoutByIndex");
            //latchId = getLatch(key);
            redisService.addZsetItemsAndTrimByIndex(key, addRecords, keepRecords, -1, reverse);
            return result;
        } finally {
            releaseLatch(key, latchId);
        }
    }

    @Around("@annotation(anno)")
    @Order(100)
    public Object zsetItemWeedoutByIndexCommon(ProceedingJoinPoint pjp, ZsetItemWeedoutByIndex anno) throws Throwable {
        String latchId = null;
        String key = null;
        String itemId;
        Double itemScore;
        Object result;
        boolean reverse;
        long keepRecords;
        try {
            anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();

            reverse = anno.reverse();
            keepRecords = anno.keepRecords();
            //如果指定了keepRecordsSpEl，则解析此表达式的值，并覆盖keepRecords指定的值
            if (!"".equals(anno.keepRecordsSpEl())) {
                keepRecords = AspectUtils.parseKey(anno.keepRecordsSpEl(), jpMethod, args, Long.class);
            }

            itemId = AspectUtils.parseKey(anno.itemIdEl(), jpMethod, args, String.class);

            itemScore = anno.itemScore();
            if (!"".equals(anno.itemScoreEl())) {
                itemScore = AspectUtils.parseKey(anno.itemScoreEl(), jpMethod, args, Double.class);
            }

            redisService.addZsetItemsAndTrimByIndex(key, Arrays.asList(ZSetItemBean.apply(itemId, itemScore)), keepRecords, -1, reverse);
            logger.trace("add zset item to redis, redis key:" + key + "keep the zset length to " + keepRecords);
            result = pjp.proceed();
            logger.trace("run jpMethod: " + jpMethod.getName() + "by @ZsetItemWeedoutByIndex");
            return result;
        } finally {
            releaseLatch(key, latchId);
        }
    }

    @Around("@annotation(anno)")
    public Object hsetPutCommon(ProceedingJoinPoint pjp, HsetPut anno) throws Throwable {
        String key = null;
        Object result;
        Map<String, Object> putMap = new HashMap<>();

        anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
        Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
        Object[] args = triple.getLeft();
        Method jpMethod = triple.getMiddle();
        key = triple.getRight();

        putMap = AspectUtils.parseKey(anno.map(), jpMethod, args, putMap.getClass());

        redisService.hsetPut(key, putMap);
        logger.trace("put hset item to redis, redis key:" + key + ", items:" + putMap);
        result = pjp.proceed();
        logger.debug("run jpMethod: " + jpMethod.getName() + "by @HsetPut");

        return result;
    }

    @Around("@annotation(anno)")
    public Object hsetGetItemCommon(ProceedingJoinPoint pjp, HsetGetItem anno) throws Throwable {

        String key = null;
        String field = null;
        Object result;
        String latchId = null;

        try {
            anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();

            field = AspectUtils.parseKey(anno.field(), jpMethod, args);
            Assert.notNull(key, "field cannot be null");

            result = redisService.hsetGet(key, field);
            if (result == null) {
                if (!redisService.hexists(key, field)) {
                    latchId = getLatch(key + ":" + field);
                    result = pjp.proceed();
                    logger.trace("run jpMethod: " + jpMethod.getName() + "by @HsetGetItem");
                    redisService.hsetPut(key, field, result);
                }
            }
            logger.trace("redis key:" + key + ", field:" + field + ":" + result);

        } finally {
            releaseLatch(key + ":" + field, latchId);
        }
        return result;
    }

    @Around("@annotation(anno)")
    public Object dsetDelItemCommon(ProceedingJoinPoint pjp, HsetDelItem anno) throws Throwable {
        String key = null;
        String field = null;
        Object result;
        String latchId = null;
        try {
            anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();

            field = AspectUtils.parseKey(anno.field(), jpMethod, args);
            Assert.notNull(key, "field cannot be null");

            //latchId = getLatch(key + ":" + field);
            redisService.hsetDelItem(key, field);
            logger.trace("delete hset item from db, redis key:" + key + ", field:" + field);
            result = pjp.proceed();
            logger.trace("run jpMethod: " + jpMethod.getName() + "by @HsetDelItem");
        } finally {
            releaseLatch(key + ":" + field, latchId);
        }

        return result;
    }

    @Around("@annotation(anno)")
    public Object hsetPutItemCommon(ProceedingJoinPoint pjp, HsetPutItem anno) throws Throwable {
        String key = null;
        String field = null;
        Object result;
        String latchId = null;
        try {
            anno = AnnotationUtils.getAnnotation(anno, anno.getClass());
            Triple<Object[], Method, String> triple = resolve(pjp, anno.cacheKey());
            Object[] args = triple.getLeft();
            Method jpMethod = triple.getMiddle();
            key = triple.getRight();
            field = AspectUtils.parseKey(anno.field(), jpMethod, args);
            Assert.notNull(key, "field cannot be null");
            Object hsetItem = AspectUtils.parseKey(anno.hsetItemEl(), jpMethod, args, Object.class);

            //latchId = getLatch(key + ":" + field);
            redisService.hsetPut(key, field, hsetItem);
            logger.trace("put hset item to redis, redis key:" + key + ", field:" + field);
            result = pjp.proceed();
            logger.trace("run jpMethod: " + jpMethod.getName() + "by @HsetPutItem");
        } finally {
            releaseLatch(key + ":" + field, latchId);
        }

        return result;
    }

    private String getLatch(String key) {
        return latchArbitrator.grabLatch(key);
    }

    private void releaseLatch(String key, String latchId) {
        if (latchId != null) {
            latchArbitrator.releaseLatch(key, latchId);
        }
    }

    /**
     * 解析出切面方法中必须的参数
     */
    private Triple<Object[], Method, String> resolve(ProceedingJoinPoint pjp, String keySpEl) {
        Object[] args = pjp.getArgs();
        Method jpMethod = AspectUtils.getMethod(pjp);
        String key = AspectUtils.parseKey(keySpEl, jpMethod, args);
        Assert.notNull(key, "cacheKey cannot be null ");
        return Triple.of(args, jpMethod, key);
    }

}
