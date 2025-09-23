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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.util;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

/**
 * CalendarUtils using ICU4J Persian calendar
 * 
 * This replaces java.util.Calendar with com.ibm.icu.util.Calendar
 * so that Persian calendar is supported transparently.
 * 
 * @author Adapted
 */
public class CalendarUtils {
	
	private static final ULocale PERSIAN = new ULocale("fa_IR@calendar=persian");

	/**
	 * Check if a string is a valid date
	 * @param date String to be checked
	 * @param dateFormat format of the date e.g. MM/dd/yyyy - see SimpleDateFormat
	 * @return true if it is a valid date
	 */
	@Deprecated
	public static boolean isValidDate(String date, String dateFormat) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.setLenient(false);
			sdf.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	
	/**
	 * Parse a string to give a Date object
	 * @param date
	 * @param dateFormat format of the date e.g. MM/dd/yyyy - see SimpleDateFormat
	 * @return null if not a valid date
	 */
	public static Date getDate(String date, String dateFormat) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.setLenient(false);
			return sdf.parse(date);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int date2dayOfYear(int sessionYear, Date meetingDate) {
		Calendar c = Calendar.getInstance(PERSIAN);
		c.setTime(meetingDate);
		int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
		if (c.get(Calendar.YEAR) < sessionYear) {
			Calendar x = Calendar.getInstance(PERSIAN);
		    x.set(c.get(Calendar.YEAR), Calendar.DECEMBER, 31, 0, 0, 0);
		    x.clear(Calendar.MILLISECOND);
		    dayOfYear -= x.get(Calendar.DAY_OF_YEAR);
		} else if (c.get(Calendar.YEAR) > sessionYear) {
			Calendar x = Calendar.getInstance(PERSIAN);
		    x.set(sessionYear, Calendar.DECEMBER, 31, 0, 0, 0);
		    x.clear(Calendar.MILLISECOND);
		    dayOfYear += x.get(Calendar.DAY_OF_YEAR);
		}
		return dayOfYear;
	}
	
	public static Date dateOfYear2date(int sessionYear, int dayOfYear) {
		Calendar c = Calendar.getInstance(PERSIAN);
		c.set(sessionYear, Calendar.DECEMBER, 31, 0, 0, 0);
		c.clear(Calendar.MILLISECOND);
		if (dayOfYear <= 0) {
			c.set(Calendar.YEAR, sessionYear - 1);
			dayOfYear += c.get(Calendar.DAY_OF_YEAR);
		} else if (dayOfYear > c.get(Calendar.DAY_OF_YEAR)) {
			dayOfYear -= c.get(Calendar.DAY_OF_YEAR);
			c.set(Calendar.YEAR, sessionYear + 1);
		}
		c.set(Calendar.DAY_OF_YEAR, dayOfYear);
		return c.getTime();
	}
}

