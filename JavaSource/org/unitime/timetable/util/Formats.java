package org.unitime.timetable.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

/**
 * To prevent concurrency issues and to promote localization, all date and number formating needs
 * should be routed through this class.
 */
public class Formats {
    private static StudentSectioningConstants SCT_CONSTANTS = Localization.create(StudentSectioningConstants.class);
    private static GwtConstants GWT_CONSTANTS = Localization.create(GwtConstants.class);

    private static final ThreadLocal<FormatBundle> sBundle = ThreadLocal.withInitial(FormatBundle::new);

    public enum Pattern implements Serializable {
        DATE_DAY_OF_WEEK("EEE");

        private String pattern;

        Pattern(String pattern) { this.pattern = pattern; }
        public String toPattern() { return pattern; }
    }

    public static Format<Date> getDateFormat(final String pattern) {
        return new Format<Date>() {
            @Override
            public String format(Date t) { return getFormats().getDateFormat(pattern).format(t); }
            @Override
            public Date parse(String source) throws ParseException { return getFormats().getDateFormat(pattern).parse(source); }
            @Override
            public String toPattern() { return pattern; }
            @Override
            public boolean isValid(String source) {
                try { return parse(source) != null; } catch (Throwable t) { return false; }
            }
        };
    }

    public static FormatBundle getFormats() { return sBundle.get(); }

    public static class FormatBundle {
        private Map<String, DateFormat> iDateFormats = new Hashtable<>();
        private Map<String, NumberFormat> iNumberFormats = new Hashtable<>();

        public DateFormat getDateFormat(String pattern) {
            DateFormat df = iDateFormats.get(pattern);
            if (df == null) {
                if ("fa".equalsIgnoreCase(Localization.getJavaLocale().getLanguage())) {
                    ULocale persian = new ULocale("fa_IR@calendar=persian");
                    df = new ICUDateFormatWrapper(new SimpleDateFormat(pattern.replace('e','E'), persian));
                } else {
                    df = new java.text.SimpleDateFormat(pattern, Localization.getJavaLocale());
                }
                iDateFormats.put(pattern, df);
            }
            return df;
        }

        public NumberFormat getNumberFormat(String pattern) {
            NumberFormat nf = iNumberFormats.get(pattern);
            if (nf == null) {
                nf = new DecimalFormat(pattern, new DecimalFormatSymbols(Localization.getJavaLocale()));
                iNumberFormats.put(pattern, nf);
            }
            return nf;
        }
    }

    public static interface Format<T> extends Serializable {
        String format(T t);
        T parse(String source) throws ParseException;
        String toPattern();
        boolean isValid(String source);
    }

    /** Wrapper to convert ICU SimpleDateFormat to java.text.DateFormat */
    private static class ICUDateFormatWrapper extends DateFormat {
        private final SimpleDateFormat icuFormat;
        ICUDateFormatWrapper(SimpleDateFormat icuFormat) { this.icuFormat = icuFormat; }
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition fieldPosition) {
            return toAppendTo.append(icuFormat.format(date));
        }
        @Override
        public Date parse(String source, java.text.ParsePosition pos) {
            try {
                Date d = icuFormat.parse(source);
                pos.setIndex(source.length());
                return d;
            } catch (Exception e) {
                pos.setErrorIndex(0);
                return null;
            }
        }
        @Override
        public Object clone() { return new ICUDateFormatWrapper((SimpleDateFormat)icuFormat.clone()); }
    }
}

