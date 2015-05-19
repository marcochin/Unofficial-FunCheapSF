package com.chin.marco.uofuncheapsf.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chin.marco.uofuncheapsf.R;
import com.chin.marco.uofuncheapsf.customclasses.MyCustomMapView;
import com.chin.marco.uofuncheapsf.interfaces.FragmentCommunicator;
import com.chin.marco.uofuncheapsf.pojo.Event;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by Marco on 4/22/2015.
 */
public class GoogleMapFragment extends Fragment{
    private static final LatLng SAN_FRANCISCO = new LatLng(37.7577 ,-122.4376);

    private GoogleMap mGoogleMap;
    private FragmentCommunicator mActivity;
    private MyCustomMapView mMapView;
    private FloatingActionsMenu mFloatingActionMenu;
    private ProgressWheel mProgressWheel;

    private boolean mIsMapPanning = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentCommunicator)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getTag returns "android:switcher:2131296349:1"
        mFloatingActionMenu = mActivity.getFloatingActionMenu();

        mMapView = new MyCustomMapView(getActivity(), this, mFloatingActionMenu);
        mMapView.onCreate(savedInstanceState);

        //You need this line or it will throw this error: java.lang.NullPointerException: CameraUpdateFactory is not initialized
        MapsInitializer.initialize(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_google_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressWheel = (ProgressWheel)view.findViewById(R.id.progress_wheel);
        ViewGroup mapLinearLayout = (ViewGroup)view.findViewById(R.id.map_linear_layout);
        mapLinearLayout.addView(mMapView);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;

                mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        mIsMapPanning = false;

                        //make the floating action menu visible only if finger is off map AND map is stopped panning
                        if(mMapView.isFingerOffMap() && mFloatingActionMenu.getVisibility() == View.INVISIBLE)
                            mFloatingActionMenu.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        //Find the location button and position it at the bottom left
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

        // position on bottom left
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rlp.setMargins(32, 0, 0, 45);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.updateFragmentDelegate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void panToSF(){
        if(mGoogleMap != null) {
            //You can zoom anywhere between [0 and 21]. Higher number is more zoom
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(SAN_FRANCISCO, 12f);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    public boolean isMapPanning(){
        return mIsMapPanning;
    }

    public void setIsMapPanning(boolean panning){
        mIsMapPanning = panning;
    }

    /*public void populateEventMarkers(){
        if(mGoogleMap != null) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(10, 10))
                    .title("Hello world"));
        }
    }*/

    public void loadMapMarker(LatLng latLng, Event event){
        if(latLng == null)
            return;

        if(mGoogleMap != null) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(event.getLocation()));
        }
    }

    public void enableMyLocation(boolean enabled){
        //TODO make finding location a little bit more efficient cause GPS turns on every 3 seconds
        if(mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(enabled);
        }

    }

    public ProgressWheel getProgressWheel(){
        return mProgressWheel;
    }

}
