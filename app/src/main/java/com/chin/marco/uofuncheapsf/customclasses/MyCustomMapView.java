package com.chin.marco.uofuncheapsf.customclasses;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.chin.marco.uofuncheapsf.fragments.GoogleMapFragment;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.MapView;

/**
 * Created by Marco on 4/27/2015.
 */
public class MyCustomMapView extends MapView {
    private GestureDetector mGestureDetector;
    private FloatingActionsMenu mFloatingActionMenu;
    private GoogleMapFragment mGoogleMapFragment;

    private boolean mIsFingerOffMap = true;

    public MyCustomMapView(Context context, GoogleMapFragment googleMapFragment, final FloatingActionsMenu floatingActionMenu) {
        super(context);

        mGoogleMapFragment = googleMapFragment;
        mFloatingActionMenu = floatingActionMenu;

        mGestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if(mFloatingActionMenu.getVisibility() == VISIBLE) {
                    mFloatingActionMenu.setVisibility(INVISIBLE);
                    mGoogleMapFragment.setIsMapPanning(true);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if(mFloatingActionMenu.getVisibility() == VISIBLE) {
                    mFloatingActionMenu.setVisibility(INVISIBLE);
                    mGoogleMapFragment.setIsMapPanning(true);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mIsFingerOffMap = false;
                break;
            case MotionEvent.ACTION_UP:
                mIsFingerOffMap = true;

                //make the floating action menu visible only if finger is off map AND map is stopped panning
                if(!mGoogleMapFragment.isMapPanning() && mFloatingActionMenu.getVisibility() == INVISIBLE) {
                    mFloatingActionMenu.setVisibility(VISIBLE);
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean isFingerOffMap(){
        return mIsFingerOffMap;
    }
}
