package com.collie.bgEra.commons.util;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import java.util.List;

public class StringUtils {
    private static Logger logger = Logger.getLogger(StringUtils.class);

    protected StringUtils() {
    }

    public static Integer toInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            logger.warn("StringUtils parse number failed:" + s, e);
            return null;
        }
    }

    public static Float toFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            logger.warn("StringUtils parse number failed:" + s, e);
            return null;
        }
    }

    public static Double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            logger.warn("StringUtils parse number failed:" + s, e);
            return null;
        }
    }

    public static Long toLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            logger.warn("StringUtils parse number failed:" + s, e);
            return null;
        }
    }

    public static boolean isEmpty(String text) {
        return org.apache.commons.lang3.StringUtils.isEmpty(text);
    }

    public static boolean isBlank(String text) {
        return org.apache.commons.lang3.StringUtils.isBlank(text);
    }

    public static boolean isNotBlank(String text) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(text);
    }

    public static String capitalize(String text) {
        return org.apache.commons.lang3.StringUtils.capitalize(text);
    }

    public static String substring(String text, int offset, int limit) {
        return org.apache.commons.lang3.StringUtils.substring(text, offset,
                limit);
    }

    public static String substringBefore(String text, String token) {
        return org.apache.commons.lang3.StringUtils
                .substringBefore(text, token);
    }

    public static String substringAfter(String text, String token) {
        return org.apache.commons.lang3.StringUtils.substringAfter(text, token);
    }

    public static String[] splitByWholeSeparator(String text, String separator) {
        return org.apache.commons.lang3.StringUtils.splitByWholeSeparator(text,
                separator);
    }

    @SuppressWarnings("rawtypes")
    public static String join(List list, String separator) {
        return org.apache.commons.lang3.StringUtils.join(list, separator);
    }

    public static String escapeHtml(String text) {
        return StringEscapeUtils.escapeHtml4(text);
    }

    public static String unescapeHtml(String text) {
        return StringEscapeUtils.unescapeHtml4(text);
    }

    public static String escapeXml(String text) {
        return StringEscapeUtils.escapeXml11(text);
    }

    public static String unescapeXml(String text) {
        return StringEscapeUtils.unescapeXml(text);
    }
}
