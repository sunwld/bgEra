package com.collie.bgEra.commons.util;


import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;

public class CommonUtils {
    /**
     * 根据流水号类型得到流水号
     *
     * @param typeId
     * @return
     */
    public static String getSerialNO(String typeId) {
        return "";
    }

    /**
     * 根据参数编号获取对应的系统参数内容
     *
     * @param itemId
     * @return
     */
    public static String getSystemParam(String itemId) {
        return "";
    }

    /**
     * 获取当前登录的用户对应的语言环境
     *
     * @return
     */
    public static String getI18Msg(String msgId) {
//		I18nMessagesService i18nMessagesService = ContextHolder.getBean("I18nUtils");
//		return  i18nMessagesService.getI18Msg(msgId);
        return "";
    }

    public static Properties readPropertiesFile(String path) throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(path));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
