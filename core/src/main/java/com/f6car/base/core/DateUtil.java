/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.core;

import com.f6car.base.constant.Constants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

/**
 * @author qixiaobo
 */
public final class DateUtil {
    private DateUtil() {
    }

    public String getDateFormat(Date date) {
        return getDateFormat(date, Constants.DEFAULT_DATE_FORMAT_PATTERN);
    }

    public String getDateFormat(Date date, String format) {
        return new DateTime(date).toString(format);

    }

    public Date parseDate(String dateStr) {
        return parseDate(dateStr, Constants.DEFAULT_DATE_FORMAT_PATTERN);
    }

    public Date parseDate(String dateStr, String format) {
        return DateTimeFormat.forPattern(format).parseDateTime(format).toDate();
    }

    public Date addDay(Date date, int day) {
        return new DateTime(date).plusDays(day).toDate();
    }

    public Date addMonth(Date date, int month) {
        return new DateTime(date).plusMonths(month).toDate();
    }

    public Date addHour(Date date, int hour) {
        return new DateTime(date).plusHours(hour).toDate();
    }

}
