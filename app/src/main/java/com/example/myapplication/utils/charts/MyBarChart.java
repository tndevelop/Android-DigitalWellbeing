package com.example.myapplication.utils.charts;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.os.Build;

import com.example.myapplication.R;
import com.example.myapplication.utils.AppUsageComparator;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MyBarChart {

    BarChart barChart;
    Context context;

    public MyBarChart(BarChart barChart, Context context) {
        this.barChart = barChart;
        this.context = context;
    }

    public void initChart(){

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawLabels(false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = barChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        barChart.getDescription().setEnabled(false);

        barChart.setHighlightPerTapEnabled(true);

        barChart.animateY(1400, Easing.EaseInOutQuad);

    }

    public void drawScreenTimeChart(List<UsageEvents.Event> events) {
        barChart.clear();
        barChart.invalidate(); // refresh
        barChart.clear();        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        events.sort(new AppUsageComparator.TimeStampComparatorAsc());

        HashMap<Integer, List<UsageEvents.Event>> splittedEvents = new HashMap<>();

        //split events by hours
        for(UsageEvents.Event event : events){
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(event.getTimeStamp());
            Integer hour = c.get(Calendar.HOUR_OF_DAY);
            if(splittedEvents.get(hour) == null) splittedEvents.put(hour, new LinkedList<>());
            splittedEvents.get(hour).add(event);
        }

        //sum the usage time for each hour
        boolean continueFromPrevious = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (int i = 0; i < 24; i++) {
                List<UsageEvents.Event> hourEvents = splittedEvents.get(i);
                if (hourEvents == null) {
                    //no data for the given hour
                    yVals1.add(new BarEntry(i, 0));
                } else {
                    //count usage
                    long usage = 0;
                    long activityStart = 0, activityStop;
                    boolean monitor = false;

                    for (UsageEvents.Event event : hourEvents) {

                        if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED && !monitor) {
                            //start monitoring
                            monitor = true;
                            activityStart = event.getTimeStamp();
                        } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED && continueFromPrevious && !monitor) {
                            Calendar temp = Calendar.getInstance();
                            temp.setTimeInMillis(event.getTimeStamp());
                            temp.set(Calendar.MINUTE, 0);
                            temp.set(Calendar.SECOND, 0);
                            temp.set(Calendar.MILLISECOND, 0);
                            usage += (event.getTimeStamp() - temp.getTimeInMillis());
                            continueFromPrevious = false;
                        } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED && monitor && !continueFromPrevious) {
                            monitor = false;
                            activityStop = event.getTimeStamp();
                            if (activityStop > activityStart)
                                usage += (activityStop - activityStart);
                        }
                    }

                    if (monitor) {
                        Calendar temp = Calendar.getInstance();
                        temp.setTimeInMillis(activityStart);
                        temp.add(Calendar.HOUR_OF_DAY, 1);
                        temp.set(Calendar.MINUTE, 0);
                        temp.set(Calendar.SECOND, 0);
                        temp.set(Calendar.MILLISECOND, 0);
                        if (temp.before(Calendar.getInstance())) {
                            usage += (temp.getTimeInMillis() - activityStart);
                            continueFromPrevious = true;
                        }
                    } else
                        continueFromPrevious = false;

                    yVals1.add(new BarEntry(i, TimeUnit.MILLISECONDS.toMinutes(usage)));

                }
            }
        }
        else {
            for (int i = 0; i < 24; i++) {
                List<UsageEvents.Event> hourEvents = splittedEvents.get(i);
                if (hourEvents == null) {
                    //no data for the given hour
                    yVals1.add(new BarEntry(i, 0));
                } else {
                    //count usage
                    long usage = 0;
                    long activityStart = 0, activityStop;
                    boolean monitor = false;

                    for (UsageEvents.Event event : hourEvents) {

                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && !monitor) {
                            //start monitoring
                            monitor = true;
                            activityStart = event.getTimeStamp();
                        } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && continueFromPrevious && !monitor) {
                            Calendar temp = Calendar.getInstance();
                            temp.setTimeInMillis(event.getTimeStamp());
                            temp.set(Calendar.MINUTE, 0);
                            temp.set(Calendar.SECOND, 0);
                            temp.set(Calendar.MILLISECOND, 0);
                            usage += (event.getTimeStamp() - temp.getTimeInMillis());
                            continueFromPrevious = false;
                        } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && monitor && !continueFromPrevious) {
                            monitor = false;
                            activityStop = event.getTimeStamp();
                            if (activityStop > activityStart)
                                usage += (activityStop - activityStart);
                        }
                    }

                    if (monitor) {
                        Calendar temp = Calendar.getInstance();
                        temp.setTimeInMillis(activityStart);
                        temp.add(Calendar.HOUR_OF_DAY, 1);
                        temp.set(Calendar.MINUTE, 0);
                        temp.set(Calendar.SECOND, 0);
                        temp.set(Calendar.MILLISECOND, 0);
                        if (temp.before(Calendar.getInstance())) {
                            usage += (temp.getTimeInMillis() - activityStart);
                            continueFromPrevious = true;
                        }
                    } else
                        continueFromPrevious = false;

                    yVals1.add(new BarEntry(i, TimeUnit.MILLISECONDS.toMinutes(usage)));

                }
            }
        }

        BarDataSet set1;

        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {

            set1 = new BarDataSet(yVals1, context.getText(R.string.label_screen_time).toString());
            set1.setDrawValues(true);
            set1.setDrawIcons(false);


            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.5f);

            barChart.setData(data);

            barChart.setMaxVisibleValueCount(10);
            barChart.setVisibleXRangeMinimum(2);
            barChart.setVisibleXRangeMaximum(10);

            Calendar now = Calendar.getInstance();
            barChart.moveViewToX(now.get(Calendar.HOUR_OF_DAY) -5);
            barChart.setDrawGridBackground(false);

            barChart.setTouchEnabled(true);
            IMarker marker = new CustomMarkerViewTimeSpent(context, R.layout.tv_details_content);
            barChart.setMarker(marker);
        }
        barChart.notifyDataSetChanged(); // let the chart know it's data changed
        barChart.invalidate(); // refresh
    }

}

