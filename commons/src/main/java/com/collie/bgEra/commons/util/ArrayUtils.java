package com.collie.bgEra.commons.util;

public class ArrayUtils {
    public static <T> T geti(T[] ts, Integer i) {
        if (i == null || i < 0 || ts == null || ts.length == 0) {
            return null;
        } else {
            return ts[i];
        }
    }
}
