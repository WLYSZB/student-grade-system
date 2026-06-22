package com.example.gradesystem.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        return dateStr == null || dateStr.isEmpty() ? null : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr == null || dateTimeStr.isEmpty() ? null : LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }
}
