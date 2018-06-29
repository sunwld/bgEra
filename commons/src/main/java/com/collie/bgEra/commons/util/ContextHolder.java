package com.collie.bgEra.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 在ApplicationContext环境外获取bean的工具类.
 */
public class ContextHolder implements ApplicationContextAware {
    private static Logger logger = LoggerFactory.getLogger("util");
    private static ApplicationContext ctx;

    /**
     * 初始化applicationContext
     *
     * @param applicationContext applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        if (ctx == null) {
            ctx = applicationContext;
        }
    }

    /**
     * 获得ApplicationContext.
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getCtx() {
        return ctx;
    }

    /**
     * 根据class获得bean.
     *
     * @param clz Class
     * @return T
     */
    public static <T> T getBean(Class<T> clz) {
        return ctx.getBean(clz);
    }

    /**
     * 根据id获得bean.
     *
     * @param id String
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String id) throws BeansException {
        return (T) ctx.getBean(id);
    }

    public static void clearCtx() {
        if (ctx != null) {
            ctx = null;
        }
    }

    /**
     * @param id spring bean name
     * @return any type of spring bean
     * @note 如果你的BEAN NAME并不存在，并不会报错会获得一个NULL类型的对象
     */
    public static <T> T getBeanSafe(String id) {
        try {
            return (T) ctx.getBean(id);
        } catch (BeansException ex) {
            logger.warn(id + " can not be found in spring context,will return null.");
            return null;
        }
    }

    /**
     *
     * @param clz
     * @param <T>
     * @return
     * @note 如果你的BEAN NAME并不存在，并不会报错会获得一个NULL类型的对象
     */
    public static <T> T getBeanSafe(Class<T> clz) {
        try {
            return (T) ctx.getBean(clz);
        } catch (BeansException ex) {
            logger.warn(clz + " can not be found in spring context,will return null.");
            return null;
        }
    }
}
