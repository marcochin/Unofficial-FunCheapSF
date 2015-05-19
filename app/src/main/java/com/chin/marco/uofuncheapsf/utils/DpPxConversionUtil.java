package com.chin.marco.uofuncheapsf.utils;

import android.content.Context;

/**
 * Created by Marco on 3/28/2015.
 */
public class DpPxConversionUtil {

    public static float convertDpToPixel(float dp, Context context){
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float convertPixelsToDp(float px, Context context){
        return px / context.getResources().getDisplayMetrics().density;
    }
}