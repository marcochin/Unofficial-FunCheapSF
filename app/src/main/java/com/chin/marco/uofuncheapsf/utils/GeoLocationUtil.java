package com.chin.marco.uofuncheapsf.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by Marco on 4/24/2015.
 */
public class GeoLocationUtil {
    private static final String ERROR_TAG = "GeoLocationUtil";
    private static final String BAY_AREA = ", San Francisco, CA";

    public static LatLng getLocationFromAddress(Context context, String strAddress) {
        //If strAddress is "Golden Gate Bridge" it will be "Golden Gate Bridge, Bay Area"

        if(strAddress.isEmpty())
            return null;

        strAddress += BAY_AREA;

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address.isEmpty()) {
                Log.d("geocoder", strAddress);
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException e) {
            Log.e(ERROR_TAG, "" + e.getMessage());
            Log.e(ERROR_TAG, Log.getStackTraceString(e));
        }

        return p1;
    }
}
