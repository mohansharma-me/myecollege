package com.samratinfosys.myecollege.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by iAmMegamohan on 22-04-2015.
 */
public class MyScrollView extends ScrollView {

    public interface MyScrollViewListener {
        void onScroll(MyScrollView myScrollView, int x, int y, int old_x, int old_y);
    }

    private MyScrollViewListener m_myScrollViewListener=null;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setMyScrollViewListener(MyScrollViewListener myScrollViewListener) {
        this.m_myScrollViewListener=myScrollViewListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if(m_myScrollViewListener!=null) {
            m_myScrollViewListener.onScroll(this,l,t,oldl,oldt);
        }
    }
}
