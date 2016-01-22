/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.util;

import ch.icclab.cyclops.load.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Author: Srikanta
 * Created on: 24-Nov-14
 * Description: Utility class related to date time manipulation
 * Change Log
 * Name        Date     Comments
 */
public class DateTimeUtil {
    final static Logger logger = LogManager.getLogger(DateTimeUtil.class.getName());

    /**
     * Generates the 1 hr time range (from and to) by computing the present server time
     *
     * @return dateTime A string consisting of from and to dateTime entries
     */
    public String[] getRange() {
        String from, to;
        String sMonth = null;
        String sDay = null;
        String stHour = null;
        String sHour = null;
        String sMin = null;
        String sSec = null;
        int year, month, day, hour, min, sec, tHour;
        String[] dateTime = new String[2];

        LocalDateTime currentDate = LocalDateTime.now();
        year = currentDate.getYear();
        month = currentDate.getMonthOfYear();
        if (month <= 9) {
            sMonth = "0" + month;
        } else {
            sMonth = month + "";
        }
        day = currentDate.getDayOfMonth();
        if (day <= 9) {
            sDay = "0" + day;
        } else {
            sDay = day + "";
        }
        hour = currentDate.getHourOfDay();
        if (hour <= 9) {
            sHour = "0" + hour;
        } else {
            sHour = hour + "";
        }
        min = currentDate.getMinuteOfHour();
        if (min <= 9) {
            sMin = "0" + min;
        } else {
            sMin = min + "";
        }
        sec = currentDate.getSecondOfMinute();
        if (sec <= 9) {
            sSec = "0" + sec;
        } else {
            sSec = sec + "";
        }

        to = year + "-" + sMonth + "-" + sDay + "T" + sHour + ":" + sMin + ":" + sSec;
        Date dateTo = getDate(to);
        //Converting to in UTC
        to = getString(dateTo);
        long schedulerFrequency = Long.parseLong(Loader.getSettings().getSchedulerSettings().getSchedulerFrequency());

        long fromTimestamp = dateTo.getTime() - schedulerFrequency * 1000;
        Date fromDate = new Date(fromTimestamp);

        from = getString(fromDate);

        dateTime[0] = to;
        dateTime[1] = from;

        return dateTime;
    }


    public Date getDate(String date) {
        long epochValue = 0;
        Date dateTime = null;
        SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            dateTime = dF.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public String getString(Date date) {
        SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dF.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dF.format(date);
    }

    public long getEpoch(String date) {
        long epochValue = 0;
        Date dateTime = null;
        SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            dateTime = dF.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        epochValue = dateTime.getTime();
        return epochValue;
    }

    /**
     * Will compute number of milliseconds from epoch to startDate
     * @param time as string
     * @return milliseconds since epoch
     */
    public static long getMillisForTime(String time) {
        // first we have to get rid of 'T', as we need just T
        String isoFormat = time.replace("'", "");

        // then we have to create proper formatter
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                .withLocale(Locale.ROOT)
                .withChronology(ISOChronology.getInstanceUTC());

        // and now parse it
        DateTime dt = formatter.parseDateTime(isoFormat);

        return dt.getMillis();
    }
}
