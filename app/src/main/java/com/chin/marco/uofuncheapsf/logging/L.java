package com.chin.marco.uofuncheapsf.logging;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Marco on 3/4/2015.
 */
public class L {
    public static void t(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
