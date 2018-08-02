package com.collie.bgEra.commons.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * 日期处理工具类
 *
 * @author dylan_xu
 * @date Mar 11, 2012
 * @modified by
 * @modified date
 * @since JDK1.6
 */

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    // ~ Static fields/initializers  
    // =============================================  
    private static Logger logger = Logger.getLogger(DateUtils.class);
    private static String defaultDatePattern = "yyyy-MM-dd";
    private static String timePattern = "HH:mm";
    public static final String TS_FORMAT = DateUtils.getDatePattern() + " HH:mm:ss.S";
    /**
     * 日期格式yyyy-MM字符串常量
     */
    public static final String MONTH_FORMAT = "yyyy-MM";
    /**
     * 日期格式yyyy-MM-dd字符串常量
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 日期格式HH:mm:ss字符串常量
     */
    public static final String HOUR_FORMAT = "HH:mm:ss";
    /**
     * 日期格式yyyy-MM-dd HH:mm:ss字符串常量
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 某天开始时分秒字符串常量  00:00:00
     */
    public static final String DAY_BEGIN_STRING_HHMMSS = " 00:00:00";
    /**
     * 某天结束时分秒字符串常量  23:59:59
     */
    public static final String DAY_END_STRING_HHMMSS = " 23:59:59";

    /**
     * 日
     */
    public final static int INTERVAL_DAY = 1;
    /**
     * 周
     */
    public final static int INTERVAL_WEEK = 2;
    /**
     * 月
     */
    public final static int INTERVAL_MONTH = 3;
    /**
     * 年
     */
    public final static int INTERVAL_YEAR = 4;
    /**
     * 小时
     */
    public final static int INTERVAL_HOUR = 5;
    /**
     * 分钟
     */
    public final static int INTERVAL_MINUTE = 6;
    /**
     * 秒
     */
    public final static int INTERVAL_SECOND = 7;
    /**
     * date = 1901-01-01
     */
    public final static Date tempDate = new Date(new Long("-2177481952000"));
    ;

    // ~ Methods  
    // ================================================================  

    public DateUtils() {
    }

    /**
     * 获得服务器当前日期的年份
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getYear() {
        try {
            return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        } catch (Exception e) {
            logger.error("DateUtil.getYear():" + e.getMessage());
            return "";
        }
    }

    /**
     * 获得服务器当前日期的月份
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getMonth() {
        try {
            java.text.DecimalFormat df = new java.text.DecimalFormat();
            df.applyPattern("00;00");
            return df.format((Calendar.getInstance().get(Calendar.MONTH) + 1));
        } catch (Exception e) {
            logger.error("DateUtil.getMonth():" + e.getMessage());
            return "";
        }
    }

    /**
     * 获得服务器在当前月中天数
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getDay() {
        try {
            return String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            logger.error("DateUtil.getDay():" + e.getMessage());
            return "";
        }
    }

    public static String getYear(String dataString) {
        if (dataString != null && !"".equals(dataString)) {
            dataString.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "").replaceAll("/", "");
            return dataString.substring(0, 4);
        }

        return dataString;
    }

    public static String getMonth(String dataString) {
        if (dataString != null && !"".equals(dataString)) {
            dataString.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "").replaceAll("/", "");
            return dataString.substring(4, 6);
        }

        return dataString;
    }

    /**
     * 获得服务器当前日期及时间，以格式为：yyyy-MM-dd HH:mm:ss的日期字符串形式返回
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getDateTime() {
        try {
            return new SimpleDateFormat(DATETIME_FORMAT).format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("DateUtil.getDateTime():" + e.getMessage());
            return "";
        }
    }

    public static String getDateTime(String format) {
        try {
            return new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("DateUtil.getDateTime():" + e.getMessage());
            return "";
        }
    }

    /**
     * 获得服务器当前日期，以格式为：yyyy-MM-dd的日期字符串形式返回
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getDate() {
        try {
            return new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("DateUtil.getDate():" + e.getMessage());
            return "";
        }
    }

    /**
     * 获得服务器当前时间，以格式为：HH:mm:ss的日期字符串形式返回
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getTime() {
        String temp = " ";
        try {
            temp += new SimpleDateFormat(HOUR_FORMAT).format(Calendar.getInstance().getTime());
            return temp;
        } catch (Exception e) {
            logger.error("DateUtil.getTime():" + e.getMessage());
            return "";
        }
    }

    /**
     * 统计时开始日期的默认值
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getStartDate() {
        try {
            return getYear() + "-01-01";
        } catch (Exception e) {
            logger.error("DateUtil.getStartDate():" + e.getMessage());
            return "";
        }
    }

    /**
     * 统计时结束日期的默认值
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getEndDate() {
        try {
            return getDate();
        } catch (Exception e) {
            logger.error("DateUtil.getEndDate():" + e.getMessage());
            return "";
        }
    }

    /**
     * 比较两个日期相差的天数
     *
     * @param date1
     * @param date2
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static int getMargin(String date1, String date2) {
        int margin;
        try {
            ParsePosition pos = new ParsePosition(0);
            ParsePosition pos1 = new ParsePosition(0);
            Date dt1 = new SimpleDateFormat(DATE_FORMAT).parse(date1, pos);
            Date dt2 = new SimpleDateFormat(DATE_FORMAT).parse(date2, pos1);
            long l = dt1.getTime() - dt2.getTime();
            margin = (int) (l / (24 * 60 * 60 * 1000));
            return margin;
        } catch (Exception e) {
            logger.error("DateUtil.getMargin():" + e.toString());
            return 0;
        }
    }

    /**
     * 比较两个日期相差的天数
     *
     * @param date1
     * @param date2
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static double getDoubleMargin(String date1, String date2) {
        double margin;
        try {
            ParsePosition pos = new ParsePosition(0);
            ParsePosition pos1 = new ParsePosition(0);
            Date dt1 = new SimpleDateFormat(DATETIME_FORMAT).parse(date1, pos);
            Date dt2 = new SimpleDateFormat(DATETIME_FORMAT).parse(date2, pos1);
            long l = dt1.getTime() - dt2.getTime();
            margin = (l / (24 * 60 * 60 * 1000.00));
            return margin;
        } catch (Exception e) {
            logger.error("DateUtil.getMargin():" + e.toString());
            return 0;
        }
    }

    /**
     * 比较两个日期相差的月数
     *
     * @param date1
     * @param date2
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static int getMonthMargin(String date1, String date2) {
        int margin;
        try {
            margin = (Integer.parseInt(date2.substring(0, 4)) - Integer.parseInt(date1.substring(0, 4))) * 12;
            margin += (Integer.parseInt(date2.substring(4, 7).replaceAll("-0",
                    "-")) - Integer.parseInt(date1.substring(4, 7).replaceAll("-0", "-")));
            return margin;
        } catch (Exception e) {
            logger.error("DateUtil.getMargin():" + e.toString());
            return 0;
        }
    }

    /**
     * 返回日期加X天后的日期
     *
     * @param date
     * @param i
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String addDay(String date, int i) {
        try {
            GregorianCalendar gCal = new GregorianCalendar(
                    Integer.parseInt(date.substring(0, 4)),
                    Integer.parseInt(date.substring(5, 7)) - 1,
                    Integer.parseInt(date.substring(8, 10)));
            gCal.add(GregorianCalendar.DATE, i);
            return new SimpleDateFormat(DATE_FORMAT).format(gCal.getTime());
        } catch (Exception e) {
            logger.error("DateUtil.addDay():" + e.toString());
            return getDate();
        }
    }


    /**
     * 返回当前时间 计算后的时间
     *
     * @param i
     * @return
     */
    public static String getDateTime(int i) {
        try {
            GregorianCalendar gCal = new GregorianCalendar();
            gCal.setTime(new Date());
            gCal.add(GregorianCalendar.DATE, i);
            return new SimpleDateFormat(DATE_FORMAT).format(gCal.getTime()) + DAY_BEGIN_STRING_HHMMSS;
        } catch (Exception e) {
            logger.error("DateUtil.addMonth():" + e.toString());
            return getDate();
        }
    }

    /**
     * 返回日期加X月后的日期
     *
     * @param date
     * @param i
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String addMonth(String date, int i) {
        try {
            GregorianCalendar gCal = new GregorianCalendar(
                    Integer.parseInt(date.substring(0, 4)),
                    Integer.parseInt(date.substring(5, 7)) - 1,
                    Integer.parseInt(date.substring(8, 10)));
            gCal.add(GregorianCalendar.MONTH, i);
            return new SimpleDateFormat(DATE_FORMAT).format(gCal.getTime());
        } catch (Exception e) {
            logger.error("DateUtil.addMonth():" + e.toString());
            return getDate();
        }
    }


    /**
     * 返回日期加X年后的日期
     *
     * @param date
     * @param i
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String addYear(String date, int i) {
        try {
            GregorianCalendar gCal = new GregorianCalendar(
                    Integer.parseInt(date.substring(0, 4)),
                    Integer.parseInt(date.substring(5, 7)) - 1,
                    Integer.parseInt(date.substring(8, 10)));
            gCal.add(GregorianCalendar.YEAR, i);
            return new SimpleDateFormat(DATE_FORMAT).format(gCal.getTime());
        } catch (Exception e) {
            logger.error("DateUtil.addYear():" + e.toString());
            return "";
        }
    }

    /**
     * 返回某年某月中的最大天
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static int getMaxDay(int iyear, int imonth) {
        int day = 0;
        try {
            if (imonth == 1 || imonth == 3 || imonth == 5 || imonth == 7
                    || imonth == 8 || imonth == 10 || imonth == 12) {
                day = 31;
            } else if (imonth == 4 || imonth == 6 || imonth == 9 || imonth == 11) {
                day = 30;
            } else if ((0 == (iyear % 4)) && (0 != (iyear % 100)) || (0 == (iyear % 400))) {
                day = 29;
            } else {
                day = 28;
            }
            return day;
        } catch (Exception e) {
            logger.error("DateUtil.getMonthDay():" + e.toString());
            return 1;
        }
    }

    /**
     * 格式化日期
     *
     * @param orgDate
     * @param Type
     * @param Span
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    @SuppressWarnings("static-access")
    public String rollDate(String orgDate, int Type, int Span) {
        try {
            String temp = "";
            int iyear, imonth, iday;
            int iPos = 0;
            char seperater = '-';
            if (orgDate == null || orgDate.length() < 6) {
                return "";
            }

            iPos = orgDate.indexOf(seperater);
            if (iPos > 0) {
                iyear = Integer.parseInt(orgDate.substring(0, iPos));
                temp = orgDate.substring(iPos + 1);
            } else {
                iyear = Integer.parseInt(orgDate.substring(0, 4));
                temp = orgDate.substring(4);
            }

            iPos = temp.indexOf(seperater);
            if (iPos > 0) {
                imonth = Integer.parseInt(temp.substring(0, iPos));
                temp = temp.substring(iPos + 1);
            } else {
                imonth = Integer.parseInt(temp.substring(0, 2));
                temp = temp.substring(2);
            }

            imonth--;
            if (imonth < 0 || imonth > 11) {
                imonth = 0;
            }

            iday = Integer.parseInt(temp);
            if (iday < 1 || iday > 31)
                iday = 1;

            Calendar orgcale = Calendar.getInstance();
            orgcale.set(iyear, imonth, iday);
            temp = this.rollDate(orgcale, Type, Span);
            return temp;
        } catch (Exception e) {
            return "";
        }
    }

    public static String rollDate(Calendar cal, int Type, int Span) {
        try {
            String temp = "";
            Calendar rolcale;
            rolcale = cal;
            rolcale.add(Type, Span);
            temp = new SimpleDateFormat(DATE_FORMAT).format(rolcale.getTime());
            return temp;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 返回默认的日期格式
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getDatePattern() {
        return defaultDatePattern;
    }

    /**
     * 测试是否是当天
     *
     * @param date - 某一日期
     * @return true-今天, false-不是
     */
    @SuppressWarnings("deprecation")
    public static boolean isToday(Date date) {
        Date now = new Date();
        boolean result = true;
        result &= date.getYear() == now.getYear();
        result &= date.getMonth() == now.getMonth();
        result &= date.getDate() == now.getDate();
        return result;
    }

    /**
     * 两个日期相减，取天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long DaysBetween(Date date1, Date date2) {
        if (date2 == null)
            date2 = new Date();
        long day = (date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
        return day;
    }

    /**
     * 比较两个日期 if date1<=date2 return true
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareDate(String date1, String date2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d1 = format.parse(date1);
            Date d2 = format.parse(date2);
            return !d1.after(d2);
        } catch (ParseException e) {
            logger.error("run compareDate failed:" + date1 + date2, e);
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * 字符型转换成日期型
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static Date dateFormat(String date, String dateFormat) {
        if (date == null || "".equals(date))
            return null;
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        if (date != null) {
            try {
                return format.parse(date);
            } catch (Exception ex) {
                logger.warn("[" + date + "]run dateFormat[" + dateFormat + "] failed.");
            }
        }
        return null;
    }

    /**
     * 字符型转换成日期型
     *
     * @param date
     * @param format
     * @return
     */
    public static Date dateFormat(String date, SimpleDateFormat format) {
        if (date == null || "".equals(date))
            return null;
        if (date != null) {
            try {
                return format.parse(date);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    /**
     * 使用默认格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     * @author Robin Chang
     */
    public static Date dateFormat(String date) {
        return dateFormat(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 使用默认格式 yyyy-MM-dd HH:mm:ss
     * 将字符串时间转换成另一个format格式的时间字符串
     *
     * @param sourceStrDate
     * @param targetFormat
     * @param targetFormat
     * @return
     */
    public static String strDateFormat(String sourceStrDate, String targetFormat) {
        return dateFormat(dateFormat(sourceStrDate), targetFormat);
    }

    /**
     * 将字符串时间转换成另一个format格式的时间字符串
     *
     * @param sourceStrDate
     * @param soruceFormat
     * @param targetFormat
     * @return
     */
    public static String strDateFormat(String sourceStrDate, String soruceFormat, String targetFormat) {
        return dateFormat(dateFormat(sourceStrDate, soruceFormat), targetFormat);
    }

    /**
     * 日期型转换成字符串
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static String dateFormat(Date date, String dateFormat) {
        if (date == null)
            return "";
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(date);
    }


    /**
     * 将指定日期按默认格式进行格式代化成字符串后输出如：yyyy-MM-dd
     *
     * @param aDate
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static final String getDate(Date aDate) {
        SimpleDateFormat df = null;
        String returnValue = "";
        if (aDate != null) {
            df = new SimpleDateFormat(getDatePattern());
            returnValue = df.format(aDate);
        }
        return (returnValue);
    }

    /**
     * 取得给定日期的时间字符串，格式为当前默认时间格式
     *
     * @param theTime
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getTimeNow(Date theTime) {
        return getDateTime(timePattern, theTime);
    }

    /**
     * 取得当前时间的Calendar日历对象
     *
     * @return
     * @throws ParseException
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public Calendar getToday() throws ParseException {
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat(getDatePattern());
        String todayAsString = df.format(today);
        Calendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(todayAsString));
        return cal;
    }

    /**
     * 由于生日增加保密属性，现决定1900为保密对应值，如果遇到1900的年份，则隐掉年份
     *
     * @param date
     * @return 不保密显示1981-12-01保密则显示`12-01
     */
    public static String birthdayFormat(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat format = null;
        if (date.before(tempDate)) {
            format = new SimpleDateFormat("MM-dd");
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd");
        }
        return format.format(date);
    }

    /**
     * 使用默认格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String dateFormat(Date date) {
        return dateFormat(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static boolean isExpiredDay(Date date1) {
        long day = (new Date().getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
        if (day >= 1)
            return true;
        else
            return false;
    }

    public static Date getStartDay(int year, int month) {
        Calendar today = Calendar.getInstance();
        today.clear();
        today.set(Calendar.YEAR, year);
        today.set(Calendar.MONTH, month - 1);
        today.set(Calendar.DAY_OF_MONTH, 1);
        return today.getTime();
    }

    public static List<Integer> getBeforeYearList(int before) {
        Calendar today = Calendar.getInstance();
        int theYear = today.get(Calendar.YEAR);
        List<Integer> list = new ArrayList<Integer>();
        for (int i = before; i >= 0; i--)
            list.add(theYear - i);

        return list;
    }

    /**
     * 增加时间
     *
     * @param interval [INTERVAL_DAY,INTERVAL_WEEK,INTERVAL_MONTH,INTERVAL_YEAR,
     *                 INTERVAL_HOUR,INTERVAL_MINUTE]
     * @param date
     * @param n        可以为负数
     * @return
     */
    public static Date dateAdd(int interval, Date date, int n) {
        long time = (date.getTime() / 1000); // 单位秒
        switch (interval) {
            case INTERVAL_DAY:
                time = time + n * 86400;// 60 * 60 * 24;
                break;
            case INTERVAL_WEEK:
                time = time + n * 604800;// 60 * 60 * 24 * 7;
                break;
            case INTERVAL_MONTH:
                time = time + n * 2678400;// 60 * 60 * 24 * 31;
                break;
            case INTERVAL_YEAR:
                time = time + n * 31536000;// 60 * 60 * 24 * 365;
                break;
            case INTERVAL_HOUR:
                time = time + n * 3600;// 60 * 60 ;
                break;
            case INTERVAL_MINUTE:
                time = time + n * 60;
                break;
            case INTERVAL_SECOND:
                time = time + n;
                break;
            default:
        }

        Date result = new Date();
        result.setTime(time * 1000);
        return result;
    }

    /**
     * 计算两个时间间隔
     *
     * @param interval [INTERVAL_DAY,INTERVAL_WEEK,INTERVAL_MONTH,INTERVAL_YEAR,
     *                 INTERVAL_HOUR,INTERVAL_MINUTE]
     * @param begin
     * @param end
     * @return
     */
    public static int dateDiff(int interval, Date begin, Date end) {
        long beginTime = (begin.getTime() / 1000); // 单位：秒
        long endTime = (end.getTime() / 1000); // 单位: 秒
        long tmp = 0;
        if (endTime == beginTime) {
            return 0;
        }

        // 确定endTime 大于 beginTime 结束时间秒数 大于 开始时间秒数
        if (endTime < beginTime) {
            tmp = beginTime;
            beginTime = endTime;
            endTime = tmp;
        }

        long intervalTime = endTime - beginTime;
        long result = 0;
        switch (interval) {
            case INTERVAL_DAY:
                result = intervalTime / 86400;// 60 * 60 * 24;
                break;
            case INTERVAL_WEEK:
                result = intervalTime / 604800;// 60 * 60 * 24 * 7;
                break;
            case INTERVAL_MONTH:
                result = intervalTime / 2678400;// 60 * 60 * 24 * 31;
                break;
            case INTERVAL_YEAR:
                result = intervalTime / 31536000;// 60 * 60 * 24 * 365;
                break;
            case INTERVAL_HOUR:
                result = intervalTime / 3600;// 60 * 60 ;
                break;
            case INTERVAL_MINUTE:
                result = intervalTime / 60;
                break;
            case INTERVAL_SECOND:
                result = intervalTime / 1;
                break;
            default:
        }

        // 做过交换
        if (tmp > 0) {
            result = 0 - result;
        }
        return (int) result;
    }

    /**
     * 将开始时间和结束时间转换为snapId格式，如果interval为：
     * [INTERVAL_DAY,INTERVAL_WEEK,INTERVAL_MONTH,INTERVAL_YEAR,
     * INTERVAL_HOUR,INTERVAL_MINUTE]
     * 其中一个，并指定了参数n，则计算两个时间差，如果时间差大于指定的 n，则将endDate减去n，作为新的 startDate
     *
     * @param startTime 开始时间 ，格式为"yyyy-MM-dd HH:mm:ss"
     * @param endTime   结束时间，格式为"yyyy-MM-dd HH:mm:ss"
     * @param interval  [0,INTERVAL_DAY,INTERVAL_WEEK,INTERVAL_MONTH,INTERVAL_YEAR,INTERVAL_HOUR,INTERVAL_MINUTE]
     *                  如果为0 则不计算时间差
     * @param n         两个时间最大可间隔的长度，单位为interval指定的参数
     * @return{"beginSnapId":beginSnapId,"endSnapId":endSnapId}
     */
    public static List<String> dateStrToSnapid(String startTime, String endTime, int interval, int n) {

        Date startDate = dateFormat(startTime);
        Date endDate = dateFormat(endTime);

        if (interval != 0) {
            int diffMinute = dateDiff(INTERVAL_DAY, startDate, endDate);
            if (diffMinute > n) {
                startDate = dateAdd(INTERVAL_DAY, endDate, -n);
            }
        }

        String beginSnapId = dateFormat(startDate, "yyyyMMddHHmmss");
        String endSnapId = dateFormat(endDate, "yyyyMMddHHmmss");

        return Arrays.asList(beginSnapId, endSnapId);
    }

    /**
     * 时间格式字符串转换为snapId字符串
     *
     * @param time
     * @return
     */
    public static String dateStrToSnapid(String time) {
        return dateStrToSnapid(time, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 时间格式字符串转换为snapId字符串
     *
     * @param time
     * @return
     */
    public static String dateStrToSnapid(String time, String format) {
        Date date = dateFormat(time, format);
        String snapId = dateFormat(date, "yyyyMMddHHmmss");

        return snapId;
    }

    public static long dateDiffMs(Date begin, Date end) {
        long beginTime = begin.getTime(); // 单位：豪秒
        long endTime = end.getTime(); // 单位: 豪秒
        long tmp = 0;
        if (endTime == beginTime) {
            return 0;
        }

        // 确定endTime 大于 beginTime 结束时间秒数 大于 开始时间秒数
        if (endTime < beginTime) {
            tmp = beginTime;
            beginTime = endTime;
            endTime = tmp;
        }

        long intervalTime = endTime - beginTime;
        long result = intervalTime;

        return result;
    }

    /**
     * 当前年份
     *
     * @return
     */
    public static int getTodayYear() {
        int yyyy = Integer.parseInt(dateFormat(new Date(), "yyyy"));
        return yyyy;
    }

    public static Date getNow() {
        return new Date();
    }

    /**
     * 判断当前日期是否在两个日期之间
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    public static boolean betweenStartDateAndEndDate(Date startDate, Date endDate) {
        boolean bool = false;
        Date curDate = new Date();
        if (curDate.after(startDate) && curDate.before(dateAdd(INTERVAL_DAY, endDate, 1))) {
            bool = true;
        }
        return bool;

    }

    /**
     * 判断当前时间是否在在两个时间之间
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    public static boolean nowDateBetweenStartDateAndEndDate(Date startDate, Date endDate) {
        boolean bool = false;
        Date curDate = new Date();
        if (curDate.after(startDate) && curDate.before(endDate)) {
            bool = true;
        }
        return bool;
    }

    /**
     * 判断当前时间是否在date之后
     *
     * @param date
     * @return
     */
    public static boolean nowDateAfterDate(Date date) {
        boolean bool = false;
        Date curDate = new Date();
        if (curDate.after(date)) {
            bool = true;
        }
        return bool;
    }

    /**
     * 判断二个日期相隔的天数,结束时间为null时，，取当前时间
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    public static int getBetweenTodaysStartDateAndEndDate(Date startDate, Date endDate) {
        int betweentoday = 0;
        if (startDate == null) {
            return betweentoday;
        }
        if (endDate == null) {
            Calendar calendar = Calendar.getInstance();
            String year = new Integer(calendar.get(Calendar.YEAR)).toString();
            String month = new Integer((calendar.get(Calendar.MONTH) + 1)).toString();
            String day = new Integer(calendar.get(Calendar.DAY_OF_MONTH)).toString();
            String strtodaytime = year + "-" + month + "-" + day;
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                endDate = formatter.parse(strtodaytime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (endDate.after(startDate)) {
            betweentoday = (int) ((endDate.getTime() - startDate.getTime()) / 86400000);
        } else {
            betweentoday = (int) ((startDate.getTime() - endDate.getTime()) / 86400000);
        }
        return betweentoday;
    }

    /**
     * 取得指定长度日期时间字符串{不含格式}
     *
     * @param format 时间格式由常量决定 8: YYMMDDHH 8位 10: YYMMDDHHmm 10位 12: YYMMDDHHmmss
     *               12位 14: YYYYMMDDHHmmss 14位 15: YYMMDDHHmmssxxx 15位 (最后的xxx
     *               是毫秒)
     */
    public static String getTime(int format) {
        StringBuffer cTime = new StringBuffer(10);
        Calendar time = Calendar.getInstance();
        int miltime = time.get(Calendar.MILLISECOND);
        int second = time.get(Calendar.SECOND);
        int minute = time.get(Calendar.MINUTE);
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int day = time.get(Calendar.DAY_OF_MONTH);
        int month = time.get(Calendar.MONTH) + 1;
        int year = time.get(Calendar.YEAR);
        if (format != 14) {
            if (year >= 2000)
                year = year - 2000;
            else
                year = year - 1900;
        }
        if (format >= 2) {
            if (format == 14)
                cTime.append(year);
            else
                cTime.append(getFormatTime(year, 2));
        }
        if (format >= 4)
            cTime.append(getFormatTime(month, 2));
        if (format >= 6)
            cTime.append(getFormatTime(day, 2));
        if (format >= 8)
            cTime.append(getFormatTime(hour, 2));
        if (format >= 10)
            cTime.append(getFormatTime(minute, 2));
        if (format >= 12)
            cTime.append(getFormatTime(second, 2));
        if (format >= 15)
            cTime.append(getFormatTime(miltime, 3));
        return cTime.toString();
    }

    /**
     * 产生任意位的字符串
     *
     * @param time   要转换格式的时间
     * @param format 转换的格式
     * @return String 转换的时间
     */
    private static String getFormatTime(int time, int format) {
        StringBuffer numm = new StringBuffer();
        int length = String.valueOf(time).length();
        if (format < length)
            return null;
        for (int i = 0; i < format - length; i++) {
            numm.append("0");
        }
        numm.append(time);
        return numm.toString().trim();
    }

    public static Date getNextDay(Date date) {
        long time = (date.getTime() / 1000) + 60 * 60 * 24;
        date.setTime(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = format.parse(format.format(date));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return date;

    }

    /**
     * 将日期类转换成指定格式的字符串形式
     *
     * @param aMask
     * @param aDate
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static final String getDateTime(String aMask, Date aDate) {
        SimpleDateFormat df = null;
        String returnValue = "";

        if (aDate == null) {
            logger.error("aDate is null!");
        } else {
            df = new SimpleDateFormat(aMask);
            returnValue = df.format(aDate);
        }
        return (returnValue);
    }

    /**
     * 将指定的日期转换成默认格式的字符串形式
     *
     * @param aDate
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static final String convertDateToString(Date aDate) {
        return getDateTime(getDatePattern(), aDate);
    }

    /**
     * 将日期字符串按指定格式转换成日期类型
     *
     * @param aMask   指定的日期格式，如:yyyy-MM-dd
     * @param strDate 待转换的日期字符串
     * @return
     * @throws ParseException
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static final Date convertStringToDate(String aMask, String strDate)
            throws ParseException {
        SimpleDateFormat df = null;
        Date date = null;
        df = new SimpleDateFormat(aMask);

        if (logger.isDebugEnabled()) {
            logger.debug("converting '" + strDate + "' to date with mask '" + aMask + "'");
        }
        try {
            date = df.parse(strDate);
        } catch (ParseException pe) {
            logger.error("ParseException: " + pe);
            throw pe;
        }
        return (date);
    }

    /**
     * 将日期字符串按默认格式转换成日期类型
     *
     * @param strDate
     * @return
     * @throws ParseException
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static Date convertStringToDate(String strDate)
            throws ParseException {
        Date aDate = null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("converting date with pattern: " + getDatePattern());
            }
            aDate = convertStringToDate(getDatePattern(), strDate);
        } catch (ParseException pe) {
            logger.error("Could not convert '" + strDate + "' to a date, throwing exception");
            throw new ParseException(pe.getMessage(), pe.getErrorOffset());
        }
        return aDate;
    }

    /**
     * 返回一个JAVA简单类型的日期字符串
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static String getSimpleDateFormat() {
        SimpleDateFormat formatter = new SimpleDateFormat();
        String NDateTime = formatter.format(new Date());
        return NDateTime;
    }

    /**
     * 将指定字符串格式的日期与当前时间比较
     *
     * @param strDate 需要比较时间
     * @return <p>
     * int code
     * <ul>
     * <li>-1 当前时间 < 比较时间 </li>
     * <li> 0 当前时间 = 比较时间 </li>
     * <li>>=1当前时间 > 比较时间 </li>
     * </ul>
     * </p>
     * @author DYLAN
     * @date Feb 17, 2012
     */
    public static int compareToCurTime(String strDate) {
        if (StringUtils.isBlank(strDate)) {
            return -1;
        }
        Date curTime = Calendar.getInstance().getTime();
        String strCurTime = null;
        try {
            strCurTime = new SimpleDateFormat(DATETIME_FORMAT).format(curTime);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Could not format '" + strDate + "' to a date, throwing exception:" + e.getLocalizedMessage() + "]");
            }
        }
        if (StringUtils.isNotBlank(strCurTime)) {
            return strCurTime.compareTo(strDate);
        }
        return -1;
    }

    /**
     * @param param 目标类型Date
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Date addStartTime(Date param) {
        Date date = param;
        try {
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            return date;
        } catch (Exception ex) {
            return date;
        }
    }

    /**
     * 为查询日期添加最大时间
     *
     * @param param 目标类型Date 转换参数Date
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Date addEndTime(Date param) {
        Date date = param;
        try {
            date.setHours(23);
            date.setMinutes(59);
            date.setSeconds(0);
            return date;
        } catch (Exception ex) {
            return date;
        }
    }

    /**
     * 返回系统现在年份中指定月份的天数
     *
     * @param month 月份month
     * @return 指定月的总天数
     */
    @SuppressWarnings("deprecation")
    public static String getMonthLastDay(int month) {
        Date date = new Date();
        int[][] day = {{0, 30, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31},
                {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}};
        int year = date.getYear() + 1900;
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            return day[1][month] + "";
        } else {
            return day[0][month] + "";
        }
    }

    /**
     * 返回指定年份中指定月份的天数
     *
     * @param year  年份year
     * @param month 月份month
     * @return 指定月的总天数
     */
    public static String getMonthLastDay(int year, int month) {
        int[][] day = {{0, 30, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31},
                {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}};
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            return day[1][month] + "";
        } else {
            return day[0][month] + "";
        }
    }

    /**
     * 判断是平年还是闰年
     *
     * @param year
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    public static boolean isLeapyear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 取得当前时间的日戳
     *
     * @return
     * @author dylan_xu
     * @date Mar 11, 2012
     */
    @SuppressWarnings("deprecation")
    public static String getTimestamp() {
        Date date = Calendar.getInstance().getTime();
        String timestamp = "" + (date.getYear() + 1900) + date.getMonth()
                + date.getDate() + date.getMinutes() + date.getSeconds()
                + date.getTime();
        return timestamp;
    }

    /**
     * 取得指定时间的日戳
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String getTimestamp(Date date) {
        String timestamp = "" + (date.getYear() + 1900) + date.getMonth()
                + date.getDate() + date.getMinutes() + date.getSeconds()
                + date.getTime();
        return timestamp;
    }

//    public static void main(String[] args) {
//        System.out.println(getYear() + "|" + getMonth() + "|" + getDate());
//    }
}  