package com.collie.bgEra.commons.util;

import java.util.Date;

public class SerialNumberUtils {
    private static final String dateFormatStr = "yyyyMMddHHmmss";

    /**
     * @return 20180629124536
     */
    public static String getSerialByDateTime() {
        return DateUtils.getDateTime(dateFormatStr);
    }

    public static String getSerialByDateTime(Date date) {
        return DateUtils.dateFormat(date, dateFormatStr);
    }

    /**
     * @param serialNo
     * @return to_date 20180629124536
     */
    public static Date getDateTimeBySerial(String serialNo) {
        return DateUtils.dateFormat(serialNo, dateFormatStr);
    }

    /**
     * @param fillZero
     * @return 20180629124536 -> 20180629124530 or 2018062912453
     */
    public static String getSerialByTrunc10s(Boolean fillZero) {
        String dateStr = DateUtils.getDateTime(dateFormatStr);
        if (fillZero)
            return dateStr.substring(0, dateStr.length() - 1) + "0";
        else
            return dateStr.substring(0, dateStr.length() - 1);
    }

    public static String getSerialByTrunc10s(Date date, Boolean fillZero) {
        String dateStr = DateUtils.dateFormat(date, dateFormatStr);
        if (fillZero)
            return dateStr.substring(0, dateStr.length() - 1) + "0";
        else
            return dateStr.substring(0, dateStr.length() - 1);
    }

    /**
     * @param serialNo
     * @param fillZero
     * @return to_date 20180629124530 or 2018062912453
     */
    public static Date getDateTimeBySerialTrunc10s(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "0", dateFormatStr);
    }

    public static Date getDateTimeByTrunc10s(Date date, Boolean fillZero) {
        String str = getSerialByTrunc10s(date,fillZero);
        return getDateTimeBySerialTrunc10s(str,fillZero);
    }

    public static Date getDateTimeByTrunc10s(Date date) {
        String str = getSerialByTrunc10s(date,true);
        return getDateTimeBySerialTrunc10s(str,true);
    }

    /**
     * @param fillZero
     * @return 20180629124536 -> 20180629124500 or 201806291245
     */
    public static String getSerialByTrunc1min(Boolean fillZero) {
        if (fillZero)
            return DateUtils.getDateTime("yyyyMMddHHmm") + "00";
        else
            return DateUtils.getDateTime("yyyyMMddHHmm");
    }

    public static String getSerialByTrunc1min(Date date, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(date, "yyyyMMddHHmm") + "00";
        else
            return DateUtils.dateFormat(date, "yyyyMMddHHmm");
    }

    /**
     * @param serialNo
     * @param fillZero
     * @return 20180629124500 or 201806291245 -> date
     */
    public static Date getDateTimeBySerialTrunc1min(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "00", dateFormatStr);
    }

    public static Date getDateTimeByTrunc1min(Date date, Boolean fillZero) {
        String str = getSerialByTrunc1min(date,fillZero);
        return getDateTimeBySerialTrunc1min(str,fillZero);
    }

    public static Date getDateTimeByTrunc1min(Date date) {
        String str = getSerialByTrunc1min(date,true);
        return getDateTimeBySerialTrunc1min(str,true);
    }

    /**
     * @param fillZero
     * @return date -> 20180629124000 or 20180629124
     */
    public static String getSerialByTrunc10min(Boolean fillZero) {
        String dateStr = DateUtils.getDateTime("yyyyMMddHHmm");
        if (fillZero)
            return dateStr.substring(0, dateStr.length() - 1) + "000";
        else
            return dateStr.substring(0, dateStr.length() - 1);
    }

    public static String getSerialByTrunc10min(Date date, Boolean fillZero) {
        String dateStr = DateUtils.dateFormat(date, "yyyyMMddHHmm");
        if (fillZero)
            return dateStr.substring(0, dateStr.length() - 1) + "000";
        else
            return dateStr.substring(0, dateStr.length() - 1);
    }

    /**
     * @param serialNo
     * @param fillZero
     * @return 20180629124000 or 20180629124 -> date
     */
    public static Date getDateTimeBySerialTrunc10min(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "000", dateFormatStr);
    }

    public static Date getDateTimeBySerialTrunc10min(Date date, Boolean fillZero) {
        String str = getSerialByTrunc10min(date,fillZero);
        return getDateTimeBySerialTrunc10min(str,fillZero);
    }

    public static Date getDateTimeBySerialTrunc10min(Date date) {
        String str = getSerialByTrunc10min(date,true);
        return getDateTimeBySerialTrunc10min(str,true);
    }


    /**
     * @param fillZero
     * @return date -> 20180629120000 or 2018062912
     */
    public static String getSerialByTrunc1h(Boolean fillZero) {
        if (fillZero)
            return DateUtils.getDateTime("yyyyMMddHH") + "0000";
        else
            return DateUtils.getDateTime("yyyyMMddHH");
    }

    public static String getSerialByTrunc1h(Date date, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(date, "yyyyMMddHH") + "0000";
        else
            return DateUtils.dateFormat(date, "yyyyMMddHH");
    }

    /**
     * @param serialNo
     * @param fillZero
     * @return 20180629120000 or 2018062912 -> date
     */
    public static Date getDateTimeBySerialTrunc1h(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "0000", dateFormatStr);
    }

    public static Date getDateTimeBySerialTrunc1h(Date date, Boolean fillZero) {
        String str = getSerialByTrunc1h(date,fillZero);
        return getDateTimeBySerialTrunc1h(str,fillZero);
    }

    public static Date getDateTimeBySerialTrunc1h(Date date) {
        String str = getSerialByTrunc1h(date,true);
        return getDateTimeBySerialTrunc1h(str,true);
    }

    /**
     *
     * @param fillZero
     * @return date -> 20180629000000 or 20180629
     */
    public static String getSerialByTrunc1d(Boolean fillZero) {
        if (fillZero)
            return DateUtils.getDateTime("yyyyMMdd") + "000000";
        else
            return DateUtils.getDateTime("yyyyMMdd");
    }

    public static String getSerialByTrunc1d(Date date, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(date, "yyyyMMdd") + "000000";
        else
            return DateUtils.dateFormat(date, "yyyyMMdd");
    }

    /**
     *
     * @param serialNo
     * @param fillZero
     * @return 20180629000000 or 20180629 -> date
     */
    public static Date getDateTimeBySerialTrunc1d(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "000000", dateFormatStr);
    }

    public static Date getDateTimeBySerialTrunc1d(Date date, Boolean fillZero) {
        String str = getSerialByTrunc1d(date,fillZero);
        return getDateTimeBySerialTrunc1d(str,fillZero);
    }

    public static Date getDateTimeBySerialTrunc1d(Date date) {
        String str = getSerialByTrunc1d(date,true);
        return getDateTimeBySerialTrunc1d(str,true);
    }

    /**
     *
     * @param fillZero
     * @return date -> 20180601000000 or 20180601
     */
    public static String getSerialByTrunc1mon(Boolean fillZero) {
        if (fillZero)
            return DateUtils.getDateTime("yyyyMM") + "01000000";
        else
            return DateUtils.getDateTime("yyyyMM");
    }

    public static String getSerialByTrunc1mon(Date date, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(date, "yyyyMM") + "01000000";
        else
            return DateUtils.dateFormat(date, "yyyyMM");
    }

    /**
     *
     * @param serialNo
     * @param fillZero
     * @return 20180601000000 or 20180601 -> date
     */
    public static Date getDateTimeBySerialTrunc1mon(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "01000000", dateFormatStr);
    }

    public static Date getDateTimeBySerialTrunc1mon(Date date, Boolean fillZero) {
        String str = getSerialByTrunc1mon(date,fillZero);
        return getDateTimeBySerialTrunc1mon(str,fillZero);
    }

    public static Date getDateTimeBySerialTrunc1mon(Date date) {
        String str = getSerialByTrunc1mon(date,true);
        return getDateTimeBySerialTrunc1mon(str,true);
    }

    /**
     *
     * @param fillZero
     * @return date -> 20180101000000 or 2018
     */
    public static String getSerialByTrunc1y(Boolean fillZero) {
        if (fillZero)
            return DateUtils.getDateTime("yyyy") + "0101000000";
        else
            return DateUtils.getDateTime("yyyy");
    }

    public static String getSerialByTrunc1y(Date date, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(date, "yyyy") + "0101000000";
        else
            return DateUtils.dateFormat(date, "yyyy");
    }

    /**
     *
     * @param serialNo
     * @param fillZero
     * @return 20180101000000 or 2018 -> date
     */
    public static Date getDateTimeBySerialTrunc1y(String serialNo, Boolean fillZero) {
        if (fillZero)
            return DateUtils.dateFormat(serialNo, dateFormatStr);
        else
            return DateUtils.dateFormat(serialNo + "0101000000", dateFormatStr);
    }

    public static Date getDateTimeBySerialTrunc1y(Date date, Boolean fillZero) {
        String str = getSerialByTrunc1y(date,fillZero);
        return getDateTimeBySerialTrunc1y(str,fillZero);
    }

    public static Date getDateTimeBySerialTrunc1y(Date date) {
        String str = getSerialByTrunc1y(date,true);
        return getDateTimeBySerialTrunc1y(str,true);
    }

//    public static void main(String[] args) {
//        System.out.println(getSerialByTrunc1mon(new Date(), true));
//        System.out.println(getDateTimeBySerialTrunc1mon(getSerialByTrunc1mon(new Date(), true), true));
//    }
}
