package com.kszr.midiplayer.util;

import java.util.concurrent.TimeUnit;

/**
 * Static functions to format and display time. Specifically, the method are used to
 * convert time in milliseconds to (H...)H:MM:SS or (M)M:SS.
 * Created by abhishekchatterjee on 12/25/15.
 */
public final class TimeOperations {
    /**
     * Formats time in milliseconds to (H...)H:MM:SS or (M)M:SS.
     * @param millis Time in milliseconds
     * @return Formatted time
     */
    public static String millisToString(int millis) {
        if(millis < 0)
            throw new IllegalArgumentException("Millis cannot be negative!");
        long hour = TimeUnit.MILLISECONDS.toHours(millis);
        long minute = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hour);
        long second = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute);
        String s;
        if(hour == 0)
            s = String.format("%d:%02d", minute, second);
        else s = String.format("%d:%02d:%02d", hour, minute, second);
        return s;
    }
}
