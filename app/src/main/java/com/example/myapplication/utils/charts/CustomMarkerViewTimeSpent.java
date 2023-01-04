package com.example.myapplication.utils.charts;

import android.content.Context;
import android.widget.TextView;

import com.example.myapplication.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;


public class CustomMarkerViewTimeSpent extends MarkerView {
    private final TextView tvContent;
    Context context;


    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context the context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public CustomMarkerViewTimeSpent(Context context, int layoutResource) {
        super(context, layoutResource);
        this.context = context;
        tvContent = findViewById(R.id.tvContent);

    }
    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvContent.setText("" + (int)e.getY() + " min - h:" + (int)e.getX()); // set the entry-value as the display text

        super.refreshContent(e, highlight);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }



}
