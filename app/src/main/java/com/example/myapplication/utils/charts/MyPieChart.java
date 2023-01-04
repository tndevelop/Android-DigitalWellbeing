package com.example.myapplication.utils.charts;

import android.content.Context;
import android.graphics.Color;

import com.example.myapplication.R;
import com.example.myapplication.db.data.CustomUsageStats;
import com.example.myapplication.utils.AppUsageComparator;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.ArrayList;
import java.util.List;


public class MyPieChart {

    PieChart pieChart;
    ArrayList<Integer> colors;
    ArrayList<PieEntry> entries;
    Context context;


    public MyPieChart(PieChart pieChart, Context context) {
        this.pieChart = pieChart;
        this.context = context;
    }

    public void initChart(){
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(40, 10, 40, 10);
        pieChart.getWidth();
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(85f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1400, Easing.EaseInOutQuad);

        this.setColors();
    }

    public void setColors(){
        if(colors == null) {
            colors = new ArrayList<>();
            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);
            colors.add(ColorTemplate.getHoloBlue());
        }
    }

    public void drawScreenTimeChart(List<CustomUsageStats> customAppUsageStats){
        if(customAppUsageStats.size() > 0){
            //sort apps depending on their usage
            customAppUsageStats.sort(new AppUsageComparator.ScreenTimeComparatorDesc());

            long screenTime = 0L;
            for(CustomUsageStats appStats : customAppUsageStats){
                screenTime += appStats.getTimeInForeground();
            }

            //get the first 5 used apps
            List<CustomUsageStats> mostUsed;
            if(customAppUsageStats.size() < 5)
                mostUsed = customAppUsageStats;
            else
                mostUsed = customAppUsageStats.subList(0,4);

            entries = new ArrayList<>();

            long totalTime = 0;

            for(CustomUsageStats stats : mostUsed){
                float percentage = ((float)stats.getTimeInForeground()/(float)screenTime) * 100;

                if(percentage >= 5) {
                    entries.add(new PieEntry(percentage, stats.getAppName()));
                    totalTime += stats.getTimeInForeground();
                }
            }

            long remaining = screenTime - totalTime;
            entries.add(new PieEntry(((float)remaining/(float)screenTime) * 100, context.getString(R.string.label_others)));

            PieDataSet dataSet = new PieDataSet(entries, context.getString(R.string.label_screen_time));
            dataSet.setDrawIcons(false);

            dataSet.setSliceSpace(3f);
            dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);
            setColors();
            dataSet.setColors(colors);
            dataSet.setDrawValues(false);

            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

            PieData data = new PieData(dataSet);
            pieChart.setData(data);

            // undo all highlights
            pieChart.highlightValues(null);
            pieChart.invalidate();
            pieChart.animateY(1400, Easing.EaseInOutQuad);

        }
    }
}


