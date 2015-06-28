package com.smona.app.preinstallclient.util;

public class TimeUtils {
    private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

    public static boolean isTheSameDay(long earlier, long later) {
        return Math.abs(later - earlier) < MILLIS_PER_DAY;
    }

    public static boolean isExceedLimitDay(long earlier, long later,
            int limitDay) {
        return Math.abs(later - earlier) >= (limitDay * MILLIS_PER_DAY);
    }
}
