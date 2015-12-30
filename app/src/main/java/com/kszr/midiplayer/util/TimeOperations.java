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

    /**
     * Formats time in milliseconds to (H...)H:MM:SS or (M)M:SS.
     * @param millis Time in milliseconds
     * @param segments 0 returns an empty string
     *                 1 returns (S)S (possibly representing truncated time)
     *                 2 returns (M)M:SS (possibly representing truncated time)
     *                 3 returns (H...)H:MM:SS
     *                 Anything else throws an exception.
     * @return Formatted time
     */
    public static String millisToString(int millis, int segments) {
        if(millis < 0)
            throw new IllegalArgumentException("millis cannot be negative!");
        if(segments < 0 || segments > 3)
            throw new IllegalArgumentException("segments is an integer between 0 and 3");
        long hour = TimeUnit.MILLISECONDS.toHours(millis);
        long minute = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hour);
        long second = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute);
        String s = "";
        switch(segments) {
            case 0: break;
            case 1: s = String.format("%d", second);
                break;
            case 2: s = String.format("%d:%02d", minute, second);
                break;
            case 3: s = String.format("%d:%02d:%02d", hour, minute, second);
        }
        return s;
    }

}
