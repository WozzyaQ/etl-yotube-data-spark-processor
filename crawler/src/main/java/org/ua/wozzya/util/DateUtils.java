package org.ua.wozzya.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final String RFC3339 = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";
    public static final String yyyyMMdd = "yyyy-MM-dd";

    public static String toRfc3339(String ddMMyyyy) throws ParseException {
        SimpleDateFormat rfcFormatter = new SimpleDateFormat(RFC3339);
        SimpleDateFormat simpleFormatter = new SimpleDateFormat(yyyyMMdd);

        return rfcFormatter.format(simpleFormatter.parse(ddMMyyyy));
    }

    public static String getDateStringNowOfPattern(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static boolean compareRfc(String d1, String d2) {
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(RFC3339);
        LocalDateTime ldt1 = LocalDateTime.parse(d1, sdf);
        LocalDateTime ldt2 = LocalDateTime.parse(d2, sdf);

        return ldt1.isBefore(ldt2);
    }
}
