package com.samratinfosys.myecollege.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * Created by iAmMegamohan on 09-03-2015.
 */
public class Anim {
    public static void bootAnimation(ImageView iv) {

        AnimationSet set=new AnimationSet(true);
        set.setDuration(1000);
        set.setStartOffset(0);
        set.setRepeatCount(AnimationSet.INFINITE);
        set.setRepeatMode(AnimationSet.REVERSE);
        AlphaAnimation aa=new AlphaAnimation(0.1f,1.0f);
        aa.setRepeatCount(AnimationSet.INFINITE);
        aa.setRepeatMode(AnimationSet.REVERSE);
        set.addAnimation(aa);
        iv.startAnimation(set);

    }

    public static void loadAnimation(ImageView loadingIV, long milis, long startOffset) {

    }
}
