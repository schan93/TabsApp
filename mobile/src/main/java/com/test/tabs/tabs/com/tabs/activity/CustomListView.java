package com.test.tabs.tabs.com.tabs.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by schan on 12/12/15.
 */
public class CustomListView extends ListView {
    // last height item variable (updated from Activity)
    public int lastItemHeight = 0;

    public CustomListView(Context context) {
        super(context, null);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, final int h, int oldw, int oldh) {
        // save last position visible before resize
        final int lastPosition = super.getLastVisiblePosition();
        // call super SizeChanged method
        super.onSizeChanged(w, h, oldw, oldh);
        // after resizing, show the last visible item at the bottom of new listview's height, above the edit text
        // see : http://developer.android.com/reference/android/widget/AbsListView.html#setSelectionFromTop(int, int)
        super.setSelectionFromTop(lastPosition, (h - lastItemHeight));
    }
}
