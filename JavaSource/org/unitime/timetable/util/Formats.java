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
package org.unitime.timetable.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;

import com.ibm.icu.util.ULocale;

/**
 * To prevent concurrency issues (see bug JDK-6609686, http://bugs.sun.com/view_bug.do?bug_id=4264153) and to
 * promote localization, all date and number formating needs should be routed through this class. 
 *  
 * @author Tomas Muller
 */
public class Formats {
	private static StudentSectioningConstants SCT_CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private static GwtConstants GWT_CONSTANTS = Localization.create(GwtConstants.class);
	
	private static final ThreadLocal<FormatBundle> sBundle = new ThreadLocal<FormatBundle>() {
		 @Override
		 protected FormatBundle initialValue() {
            return new FormatBundle();
		 }
	};
	
	public enum Pattern implements Serializable {
		DATE_EXAM_PERIOD(() -> GWT_CONSTANTS.examPeriodDateFormat()),
		DATE_EVENT(() -> GWT_CONSTANTS.eventDateFormat()),
		DATE_EVENT_SHORT(() -> GWT_CONSTANTS.eventDateFormatShort()),
		DATE_EVENT_LONG(() -> GWT_CONSTANTS.eventDateFormatLong()),
		DATE_TIME_STAMP(() -> GWT_CONSTANTS.timeStampFormat()),
		DATE_TIME_STAMP_SHORT(() -> GWT_CONSTANTS.timeStampFormatShort()),
		DATE_REQUEST(() -> SCT_CONSTANTS.requestDateFormat()),
		DATE_PATTERN(() -> SCT_CONSTANTS.patternDateFormat()),
		DATE_DAY_OF_WEEK(() -> "EEE"),
		DATE_MEETING(() -> GWT_CONSTANTS.meetingDateFormat()),
		DATE_SHORT(() -> GWT_CONSTANTS.dateFormatShort()),
		TIME_SHORT(() -> GWT_CONSTANTS.timeFormatShort()),
		SESSION_DATE(() -> GWT_CONSTANTS.sessionDateFormat()),
		DATE_ENTRY_FORMAT(() -> GWT_CONSTANTS.dateEntryFormat()),
		FILTER_DATE(() -> GWT_CONSTANTS.filterDateFormat()),
		TIMETABLE_GRID_DATE(() -> GWT_CONSTANTS.timetableGridDateFormat()),
		UTILIZATION(() -> GWT_CONSTANTS.utilizationFormat());
		
		private PatternHolder iHolder;
		Pattern(PatternHolder holder) { iHolder = holder; }
		public String toPattern() { return iHolder.getPattern(); }
		protected PatternHolder holder() { return iHolder; }
	}
		
	public static void removeFormats() {
		sBundle.remove();
	}
	
	public static FormatBundle getFormats() {
		return sBundle.get();
	}
	
	public static Format<Date> getDateFormat(final String pattern) {
		return new Format<Date>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String format(Date t) {
				return getFormats().getDateFormat(pattern).format(t);
			}

			@Override
			public Date parse(String source) throws ParseException {
				return getFormats().getDateFormat(pattern).parse(source);
			}

			@Override
			public String toPattern() {
				return pattern;
			}

			@Override
			public boolean isValid(String source) {
				try {
					return parse(source) != null;
				} catch (Throwable t) {
					return false;
				}
			}
		};
	}
	
	public static Format<Number> getNumberFormat(final String pattern) {
		return new Format<Number>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String format(Number t) {
				return getFormats().getNumberFormat(pattern).format(t);
			}

			@Override
			public Number parse(String source) throws ParseException {
				return getFormats().getNumberFormat(pattern).parse(source);
			}

			@Override
			public String toPattern() {
				return pattern;
			}

			@Override
			public boolean isValid(String source) {
				try {
					return parse(source) != null;
				} catch (Throwable t) {
					return false;
				}
			}
		};
	}
	
	public static Format<Date> getDateFormat(final Pattern pattern) {
		return getDateFormat(pattern.toPattern());
	}
	
	public static Format<Number> getConcurrentNumberFormat(final Pattern pattern) {
		return getNumberFormat(pattern.toPattern());
	}
	
	public static class FormatBundle {
		private Map<String, DateFormat> iDateFormats = new Hashtable<>();
		private Map<String, NumberFormat> iNumberFormats = new Hashtable<>();
		
		private FormatBundle() {
		}
		
		public DateFormat getDateFormat(String pattern) {
			DateFormat df = iDateFormats.get(pattern);
			if (df == null) {
				if ("fa".equalsIgnoreCase(Localization.getJavaLocale().getLanguage())) {
					ULocale persian = new ULocale("fa_IR@calendar=persian");
					// جایگزینی e با E برای رفع Illegal pattern character
					df = new com.ibm.icu.text.SimpleDateFormat(pattern.replace('e', 'E'), persian);
				} else {
					df = new SimpleDateFormat(pattern, Localization.getJavaLocale());
				}
				iDateFormats.put(pattern, df);
			}
			return df;
		}
		
		public DateFormat getDateFormat(Pattern pattern) {
			return getDateFormat(pattern.toPattern());
		}
		
		public NumberFormat getNumberFormat(String pattern) {
			NumberFormat nf = iNumberFormats.get(pattern);
			if (nf == null) {
				nf = new DecimalFormat(pattern, new DecimalFormatSymbols(Localization.getJavaLocale()));
				iNumberFormats.put(pattern, nf);
			}
			return nf;
		}
		
		public NumberFormat getNumberFormat(Pattern pattern) {
			return getNumberFormat(pattern.toPattern());
		}
	}
	
	public static interface Format<T> extends Serializable {
		String format(T t);
		T parse(String source) throws ParseException;
		String toPattern();
		boolean isValid(String source);
	}
		
	protected interface PatternHolder {
		String getPattern();
	}
}

