package com.example.myapplication.utils;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import com.example.myapplication.db.data.CustomUsageStats;
import java.util.Comparator;

public class AppUsageComparator {


    /**
     * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the timestamp
     * last time the app was used in the descendant order.
     */
    public static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
        }
    }


    /**
     * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the time
     * spent in the foreground in the descendant order.
     */
    public static class ScreenTimeComparatorDesc implements Comparator<CustomUsageStats> {

        @Override
        public int compare(CustomUsageStats left, CustomUsageStats right) {
            return Long.compare(right.getTimeInForeground(), left.getTimeInForeground());
        }
    }

    /**
     * The {@link Comparator} to sort a collection of {@link UsageEvents.Event} sorted by the timestamp
     * in the ascendant order.
     */
    public static class TimeStampComparatorAsc implements Comparator<UsageEvents.Event> {

        @Override
        public int compare(UsageEvents.Event left, UsageEvents.Event right) {
            return Long.compare(left.getTimeStamp(), right.getTimeStamp());
        }
    }


}
