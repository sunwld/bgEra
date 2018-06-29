package com.collie.bgEra.commons.util;

public class RandomUtils {
    public static int generate() {
        return (int) (((Math.random() * 9) + 1) * 100000);
    }
}
