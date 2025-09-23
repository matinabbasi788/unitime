/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
/*
 * CalendarUtils - نسخه جلالی بدون نیاز به لایبرری خارجی
 * قابل استفاده به‌جای CalendarUtils اصلی برای محاسبات روز-سال جلالی (Persian/Jalali).
 */
package org.unitime.timetable.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Calendar utilities using Jalali (Persian) calendar.
 * - isValidDate(String date, String dateFormat) supports basic formats: yyyy/MM/dd, yyyy-MM-dd, dd/MM/yyyy, dd-MM-yyyy
 * - getDate(String date, String dateFormat) parses Jalali date and returns java.util.Date (Gregorian)
 * - date2dayOfYear(int sessionYear, Date meetingDate) returns day-of-year (relative to sessionYear) in Jalali
 * - dateOfYear2date(int sessionYear, int dayOfYear) returns java.util.Date (Gregorian) for given Jalali year/day-of-year
 */
public class CalendarUtils {

    /**
     * Deprecated style signature kept for compatibility.
     * This implementation expects the input date to be Jalali (Persian) in one of the supported formats.
     */
    @Deprecated
    public static boolean isValidDate(String date, String dateFormat) {
        try {
            int[] ymd = parseJalaliDate(date, dateFormat);
            // basic validation via conversion back/forth
            long jdn = jalaliToJDN(ymd[0], ymd[1], ymd[2]);
            int[] back = jdnToJalali(jdn);
            return back[0] == ymd[0] && back[1] == ymd[1] && back[2] == ymd[2];
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse a Jalali date string and return a java.util.Date (in Gregorian) or null if invalid.
     * Supported formats: "yyyy/MM/dd", "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy" (you may extend easily)
     */
    public static Date getDate(String date, String dateFormat) {
        try {
            int[] ymd = parseJalaliDate(date, dateFormat);
            long jdn = jalaliToJDN(ymd[0], ymd[1], ymd[2]);
            int[] g = jdnToGregorian(jdn);
            Calendar c = Calendar.getInstance(Locale.US);
            c.clear();
            c.set(g[0], g[1] - 1, g[2], 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static int date2dayOfYear(int sessionYear, Date meetingDate) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(meetingDate);
        int gy = c.get(Calendar.YEAR);
        int gm = c.get(Calendar.MONTH) + 1;
        int gd = c.get(Calendar.DAY_OF_MONTH);

        long jdn = gregorianToJDN(gy, gm, gd);
        int[] j = jdnToJalali(jdn);
        int jy = j[0], jm = j[1], jd = j[2];

        int dayOfYear = jalaliDayOfYear(jy, jm, jd);

        if (jy < sessionYear) {
            int prevDays = daysInJalaliYear(jy);
            dayOfYear -= prevDays;
        } else if (jy > sessionYear) {
            int sessionDays = daysInJalaliYear(sessionYear);
            dayOfYear += sessionDays;
        }
        return dayOfYear;
    }

    public static Date dateOfYear2date(int sessionYear, int dayOfYear) {
        int sessionDays = daysInJalaliYear(sessionYear);

        int year;
        int doy = dayOfYear;
        if (doy <= 0) {
            year = sessionYear - 1;
            doy += daysInJalaliYear(year);
        } else if (doy > sessionDays) {
            doy -= sessionDays;
            year = sessionYear + 1;
        } else {
            year = sessionYear;
        }

        int[] ymd = jalaliFromDayOfYear(year, doy);
        long jdn = jalaliToJDN(ymd[0], ymd[1], ymd[2]);
        int[] g = jdnToGregorian(jdn);

        Calendar c = Calendar.getInstance(Locale.US);
        c.clear();
        c.set(g[0], g[1] - 1, g[2], 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }


    /* ---------------------- Helper and conversion routines ---------------------- */

    // Parse simple Jalali date formats
    private static int[] parseJalaliDate(String date, String dateFormat) {
        if (date == null) throw new IllegalArgumentException("date==null");
        String sep = date.contains("/") ? "/" : date.contains("-") ? "-" : null;
        if (sep == null) throw new IllegalArgumentException("Unsupported date separator (expect '/' or '-')");

        String[] parts = date.split(sep);
        if (parts.length != 3) throw new IllegalArgumentException("Invalid date format");
        int y=0,m=0,d=0;

        if (dateFormat != null) {
            String fmt = dateFormat.toLowerCase();
            if (fmt.startsWith("yyyy") || fmt.startsWith("yy")) {
                // assume year/month/day
                y = Integer.parseInt(parts[0]);
                m = Integer.parseInt(parts[1]);
                d = Integer.parseInt(parts[2]);
            } else if (fmt.startsWith("dd")) {
                // assume day/month/year
                d = Integer.parseInt(parts[0]);
                m = Integer.parseInt(parts[1]);
                y = Integer.parseInt(parts[2]);
            } else {
                // fallback: try yyyy/mm/dd if first part has 4 digits
                if (parts[0].length() == 4) {
                    y = Integer.parseInt(parts[0]);
                    m = Integer.parseInt(parts[1]);
                    d = Integer.parseInt(parts[2]);
                } else {
                    d = Integer.parseInt(parts[0]);
                    m = Integer.parseInt(parts[1]);
                    y = Integer.parseInt(parts[2]);
                }
            }
        } else {
            // no format -> guess
            if (parts[0].length() == 4) {
                y = Integer.parseInt(parts[0]);
                m = Integer.parseInt(parts[1]);
                d = Integer.parseInt(parts[2]);
            } else {
                d = Integer.parseInt(parts[0]);
                m = Integer.parseInt(parts[1]);
                y = Integer.parseInt(parts[2]);
            }
        }
        return new int[] { y, m, d };
    }

    private static int jalaliDayOfYear(int jy, int jm, int jd) {
        int[] ml = jalaliMonthLengths(jy);
        int doy = 0;
        for (int i = 0; i < jm - 1; i++) doy += ml[i];
        doy += jd;
        return doy;
    }

    private static int[] jalaliFromDayOfYear(int jy, int doy) {
        int[] ml = jalaliMonthLengths(jy);
        int m = 1;
        int rem = doy;
        for (int i = 0; i < ml.length; i++) {
            if (rem <= ml[i]) {
                m = i + 1;
                break;
            } else {
                rem -= ml[i];
            }
        }
        return new int[] { jy, m, rem };
    }

    private static int[] jalaliMonthLengths(int jy) {
        int[] ml = new int[12];
        for (int i = 0; i < 6; i++) ml[i] = 31;
        for (int i = 6; i < 11; i++) ml[i] = 30;
        ml[11] = isJalaliLeap(jy) ? 30 : 29;
        return ml;
    }

    private static int daysInJalaliYear(int jy) {
        return isJalaliLeap(jy) ? 366 : 365;
    }

    private static boolean isJalaliLeap(int jy) {
        long a = jalaliToJDN(jy + 1, 1, 1) - jalaliToJDN(jy, 1, 1);
        return a == 366;
    }

    /* ---------------------- JDN conversions (jalaali-js algorithm) ---------------------- */

    private static long jalaliToJDN(int jy, int jm, int jd) {
        int epbase = jy - (jy >= 0 ? 474 : 473);
        int epyear = 474 + (epbase % 2820);
        long mdays = (jm <= 7) ? ((jm - 1) * 31L) : ((jm - 1) * 30L + 6L);
        long tmp = (long)((epyear * 682) - 110);
        long part = tmp / 2816L;
        long jdn = jd + mdays + part + (long)(epyear - 1) * 365L + (long)(epbase / 2820) * 1029983L + (1948320L - 1L);
        return jdn;
    }

    private static int[] jdnToJalali(long jdn) {
        long depoch = jdn - jalaliToJDN(475, 1, 1);
        long cycle = depoch / 1029983L;
        long cyear = depoch % 1029983L;
        long ycycle;
        if (cyear == 1029982L) {
            ycycle = 2820L;
        } else {
            long aux1 = cyear / 366L;
            long aux2 = cyear % 366L;
            ycycle = (long)((2134L * aux1 + 2816L * aux2 + 2815L) / 1028522L) + aux1 + 1L;
        }
        long jy = ycycle + 2820L * cycle + 474L;
        if (jy <= 0) jy -= 1;
        long jdn1f = jalaliToJDN((int)jy, 1, 1);
        long dayOfYear = jdn - jdn1f + 1;
        int jyI = (int) jy;
        int[] ml = jalaliMonthLengths(jyI);
        int m = 1;
        long rem = dayOfYear;
        for (int i = 0; i < ml.length; i++) {
            if (rem <= ml[i]) {
                m = i + 1;
                break;
            } else rem -= ml[i];
        }
        int d = (int) rem;
        return new int[] { jyI, m, d };
    }

    private static long gregorianToJDN(int gy, int gm, int gd) {
        int a = (14 - gm) / 12;
        int y = gy + 4800 - a;
        int m = gm + 12 * a - 3;
        long jdn = gd + ( (153 * m + 2) / 5 ) + 365L * y + y / 4 - y / 100 + y / 400 - 32045L;
        return jdn;
    }

    private static int[] jdnToGregorian(long jdn) {
        long a = jdn + 32044L;
        long b = (4L * a + 3L) / 146097L;
        long c = a - (146097L * b) / 4L;
        long d = (4L * c + 3L) / 1461L;
        long e = c - (1461L * d) / 4L;
        long m = (5L * e + 2L) / 153L;
        int day = (int) (e - (153L * m + 2L) / 5L + 1L);
        int month = (int) (m + 3L - 12L * (m / 10L));
        int year = (int) (b * 100L + d - 4800L + (m / 10L));
        return new int[] { year, month, day };
    }
}

