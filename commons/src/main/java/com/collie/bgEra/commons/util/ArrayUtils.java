package com.collie.bgEra.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayUtils {
    private static Logger logger = LoggerFactory.getLogger(ArrayUtils.class);


    public static <T> T geti(T[] ts, Integer i) {
        try {
            return ts[i];
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.warn("ArrayUtils get [" + i + "] from array,the array length is:[" + ts.length + "],return null.", e);
            return null;
        } catch (NullPointerException e) {
            logger.warn("ArrayUtils get [" + i + "] from array[" + ts + "],get NullPointerException,return null.", e);
            return null;
        }
    }

    public static <T> T getiNE(T[] ts, Integer i) {
        if (ts == null || ts.length == 0 || i >= ts.length) {
            return null;
        }
        return ts[i];
    }

    public static boolean isBlank(Object[] oa) {
        if (oa == null || oa.length == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotBlank(Object[] oa) {
        if (oa == null || oa.length == 0) {
            return false;
        }
        return true;
    }
}
