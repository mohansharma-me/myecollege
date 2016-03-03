package com.samratinfosys.myecollege.fragments;

import android.content.Intent;

/**
 * Created by iAmMegamohan on 22-04-2015.
 */
public interface OnFragmentActivityResult {
    void onFragmentResult(int requestCode, int resultCode, final Intent data);
}
