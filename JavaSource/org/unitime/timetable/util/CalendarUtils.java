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
 * CalendarUtils - نسخه جلالی
 */
package org.unitime.timetable.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.github.mfathi91.time.PersianDate;

public class CalendarUtils {

    /**
     * چک می‌کنه که رشته‌ی ورودی تاریخ جلالی معتبر هست یا نه
     * فرمت: yyyy/MM/dd
     */
    public static boolean isValidDate(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length != 3) return false;
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            PersianDate.of(year, month, day); // اگر تاریخ نامعتبر باشه exception میده
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * تبدیل رشته‌ی تاریخ (yyyy/MM/dd) به Date
     */
    public static Date getDate(String date) {
        try {
            String[] parts = date.split("/");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            PersianDate persianDate = PersianDate.of(year, month, day);
            LocalDate gregorian = persianDate.toGregorian();
            return Date.from(gregorian.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * محاسبه روز سال جلالی برای یک تاریخ
     */
    public static int date2dayOfYear(int sessionYear, Date meetingDate) {
        LocalDate gDate = meetingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        PersianDate persianDate = PersianDate.fromGregorian(gDate);

        int dayOfYear = persianDate.getDayOfYear();

        if (persianDate.getYear() < sessionYear) {
            PersianDate endPrevYear = PersianDate.of(persianDate.getYear(), 12, 29);
            dayOfYear -= endPrevYear.getDayOfYear();
        } else if (persianDate.getYear() > sessionYear) {
            PersianDate endCurrYear = PersianDate.of(sessionYear, 12, 29);
            dayOfYear += endCurrYear.getDayOfYear();
        }
        return dayOfYear;
    }

    /**
     * برگردوندن تاریخ از روی سال و روز سال (جلالی)
     */
    public static Date dateOfYear2date(int sessionYear, int dayOfYear) {
        PersianDate persianDate = PersianDate.ofYearDay(sessionYear, dayOfYear);
        LocalDate gregorian = persianDate.toGregorian();
        return Date.from(gregorian.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

