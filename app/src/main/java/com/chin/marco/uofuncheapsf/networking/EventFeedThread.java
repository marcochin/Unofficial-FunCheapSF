package com.chin.marco.uofuncheapsf.networking;

import android.app.Activity;
import android.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.chin.marco.uofuncheapsf.adapters.RecyclerViewExpandableAdapter;
import com.chin.marco.uofuncheapsf.constants.Website;
import com.chin.marco.uofuncheapsf.data.DataProvider;
import com.chin.marco.uofuncheapsf.data.SQLiteDB;
import com.chin.marco.uofuncheapsf.fragments.EventListFragment;
import com.chin.marco.uofuncheapsf.fragments.GoogleMapFragment;
import com.chin.marco.uofuncheapsf.logging.L;
import com.chin.marco.uofuncheapsf.pojo.Event;
import com.chin.marco.uofuncheapsf.utils.GeoLocationUtil;
import com.chin.marco.uofuncheapsf.utils.StringUtil;
import com.google.android.gms.maps.model.LatLng;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import it.neokree.materialtabs.MaterialTabHost;

/**
 * Created by Marco on 3/12/2015.
 */
public class EventFeedThread implements Runnable {
    private static final String ERROR_TAG = "EventFeedThread";

    private String mDateEndPoint;
    private NetworkProgressCommunicator mActivity;
    private RecyclerViewExpandableAdapter mAdapter;
    private final RecyclerView mRecyclerView;

    private DataProvider mDataProvider;
    private SQLiteDB mDB;
    private final ViewPager mViewPager;
    private final MaterialTabHost mTabHost;
    private final LinearLayout mNetWorkErrorView;
    private final LinearLayout mDataErrorView;

    private EventListFragment mEventListFragment;
    private GoogleMapFragment mGoogleMapFragment;

    private int mEventID = 0;

    public enum FragmentType{EVENT_LIST_FRAGMENT, GOOGLE_MAP_FRAGMENT}

    public EventFeedThread(NetworkProgressCommunicator activity, FragmentManager fragmentManager, String dateEndPoint){
        mEventListFragment = (EventListFragment) fragmentManager.findFragmentByTag(activity.getFragmentTag(FragmentType.EVENT_LIST_FRAGMENT));
        mGoogleMapFragment = (GoogleMapFragment) fragmentManager.findFragmentByTag(activity.getFragmentTag(FragmentType.GOOGLE_MAP_FRAGMENT));

        mActivity = activity;
        mDateEndPoint = dateEndPoint;
        mAdapter = mEventListFragment.getRecyclerViewAdapter();
        mRecyclerView = mEventListFragment.getRecyclerView();

        mDataProvider = mActivity.getDataProvider();
        mDB = mActivity.getDB();
        mViewPager = mActivity.getViewPager();
        mTabHost = mActivity.getTabHost();
        mNetWorkErrorView = mActivity.getNetworkErrorView();
        mDataErrorView = mActivity.getDataErrorView();
    }

    @Override
    public void run() {
        synchronized (mRecyclerView) {
            //Interrupt check
            if (Thread.currentThread().isInterrupted())
                return;

            hideAllErrors();
            showListProgressWheel();
            showMapProgressWheel();

            //Check if there is internet
            /*if (!isInternetAvailable()) {
                showNetworkError();
                return; //return if no internet
            }*/

            //Interrupt check
            if (Thread.currentThread().isInterrupted())
                return;

            //doInBackground
            try {
                String urlFeed = Website.FUN_CHEAP_SF_URL + mDateEndPoint;

                //retrieve the first page
                Document doc = Jsoup.connect(urlFeed)
                        .userAgent(Website.USER_AGENT)
                        .referrer(Website.GOOGLE_URL)
                        .timeout(15000)
                        .ignoreContentType(true)
                        .get();

                //Interrupt check
                if (Thread.currentThread().isInterrupted())
                    return;

                mDataProvider.clear();
                Elements eventElements = null;
                int pages = 1;

                if (doc != null) {
                    eventElements = doc.select(Website.EVENT_SELECTOR);

                    if (!eventElements.isEmpty()) {
                        for (Element eventElement : eventElements) {
                            //Interrupt check
                            if (Thread.currentThread().isInterrupted())
                                return;
                            processElement(eventElement);
                        }
                    }
                    Elements pageElement;
                    pageElement = doc.select(Website.PAGES_SELECTOR);
                    pages = pageElement.isEmpty() ? 1 : Integer.parseInt(StringUtil.getMaxPages(pageElement.get(0).ownText()));
                }
                //finished getting first page----------------------------------------------------------------

                //retrieve additional pages

                //start at 2 because we already got page 1
                for (int i = 2; i <= pages; i++) {
                    String nextPageURL = urlFeed;
                    nextPageURL += Website.PAGE_ENDPOINT + i + "/";
                    doc = Jsoup.connect(nextPageURL)
                            .userAgent(Website.USER_AGENT)
                            .referrer(Website.GOOGLE_URL)
                            .timeout(15000)
                            .ignoreContentType(true)
                            .get();

                    //Interrupt check
                    if (Thread.currentThread().isInterrupted())
                        return;

                    if (doc != null) {
                        eventElements = doc.select(Website.EVENT_SELECTOR);

                        if (!eventElements.isEmpty()) {
                            for (Element eventElement : eventElements) {

                                //Interrupt check
                                if (Thread.currentThread().isInterrupted())
                                    return;

                                processElement(eventElement);
                            }
                        }
                    }
                }
                //finished getting additonal pages----------------------------------------------------------------
                //TODO check for data error here! what if you have internet but website layout has been updated? e.g. all elements have changed
                //TODO however i think it will just return 0 elements instead of throwing an error

                //Interrupt check
                if (Thread.currentThread().isInterrupted())
                    return;

                //Load EventListFragment and remove its progress wheel
                ((Activity) mActivity).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setEventList(mDataProvider.getFilteredEventList());
                        //database tables cant have slashes
                        mDB.createTable(StringUtil.removeSlashes(mDateEndPoint), mDataProvider.getFullEventList());

                        hideListProgressWheel();
                        L.t((Activity) mActivity, "list loaded");
                    }
                });

                //now load mapMarkers
                for(final Event event: mDataProvider.getFullEventList()){
                    final LatLng eventLatLng = GeoLocationUtil.getLocationFromAddress((Activity) mActivity, event.getLocation());

                    ((Activity) mActivity).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGoogleMapFragment.loadMapMarker(eventLatLng, event);
                        }
                    });

                }

                ((Activity) mActivity).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideMapProgressWheel();
                        L.t((Activity) mActivity, "map loaded");
                    }
                });

            } catch (IOException e) {
                Log.e(ERROR_TAG, "" + e.getMessage());
                Log.e(ERROR_TAG, Log.getStackTraceString(e));

                //check for internet connection again just in case user lost internet AFTER the first check
                if (!isInternetAvailable()) {
                    showNetworkError();
                } else {
                    mActivity.setDataErrorMessage(e.getMessage() + "\n" + Log.getStackTraceString(e));
                    showDataError();
                }

                hideListProgressWheel();
                //TODO hide map Progresswheel and should hideViewPager?

            } catch (IndexOutOfBoundsException e){
                Log.e(ERROR_TAG, "" + e.getMessage());
                Log.e(ERROR_TAG, Log.getStackTraceString(e));
            }
        } //end synchronized block
    }

    public void processElement(Element eventElement) {
        Elements titleElements = null;
        Elements locationElements = null;
        Elements timeElements = null;
        Elements priceElements = null;
        Elements thumbnailURLElements = null;
        Elements urlClickElements = null;
        Elements descriptionElements = null;
        Elements caveatElements = null;
        Elements contestEndedElements = null;
        Elements contestEnterElements = null;

        String title;
        String location;
        String time;
        String price;
        String thumbnailURL;
        String urlClick;
        String description;
        String caveat;
        String contestState;
        int timeType = Event.NO_TIME;

        titleElements = eventElement.select(Website.TITLE_SELECTOR);
        locationElements = eventElement.select(Website.LOCATION_SELECTOR);
        timeElements = eventElement.select(Website.TIME_SELECTOR);
        priceElements = eventElement.select(Website.PRICE_SELECTOR);
        thumbnailURLElements = eventElement.select(Website.THUMBNAILURL_SELECTOR);
        urlClickElements = eventElement.select(Website.URLCLICK_SELECTOR);
        descriptionElements = eventElement.select(Website.DESCRIPTION_SELECTOR);
        caveatElements = eventElement.select(Website.CAVEAT_SELECTOR);
        contestEndedElements = eventElement.select(Website.CONTEST_ENDED_SELECTOR);
        contestEnterElements = eventElement.select(Website.CONTEST_ENTER_SELECTOR);

        //This is the start of setting up the parent
        //If there is no title entry is deleted, so skip
        if (!titleElements.isEmpty())
            title = titleElements.first().ownText();
        else
            return;

        time = !timeElements.isEmpty() ? StringUtil.getTime(timeElements.first().ownText()) : "";

        //Skip Every's
        if (!timeElements.isEmpty()) {
            timeType = StringUtil.getTimeType(time);//will return EVENT.NO_TIME if no type

            if ((timeType == Event.EVERY_SOMEDAY))
                return;
        }

        location = !locationElements.isEmpty() ? locationElements.first().ownText() : "";
        price = priceElements.size() > 1 ? priceElements.get(1).ownText() : !priceElements.isEmpty() ? StringUtil.getPriceFromCost(priceElements.get(0).ownText()) : "";
        thumbnailURL = !thumbnailURLElements.isEmpty() ? thumbnailURLElements.first().attr(Website.SRC_ATTRIBUTE) : "";

        //This is the start of setting up child
        //description can have multiple paragraphs
        description = "";
        if (!descriptionElements.isEmpty()) {

            for (Element descriptionElement : descriptionElements) {
                if (!descriptionElement.equals(descriptionElements.get(0)))
                    description += "\n\n";

                //maintain breaklines
                description += Jsoup.parse(descriptionElement.html().replaceAll("(?i)<br[^>]*>\\s*", "<pre>\n</pre>")).text();
            }
        }

        urlClick = !urlClickElements.isEmpty() ? urlClickElements.first().attr(Website.HREF_ATTRIBUTE) : "";
        caveat = !caveatElements.isEmpty() ? caveatElements.first().ownText() : "";

        if(!contestEnterElements.isEmpty())
            contestState = contestEnterElements.first().ownText();
        else if(!contestEndedElements.isEmpty())
            contestState = contestEndedElements.first().ownText();
        else
            contestState = "";

        Event event = new Event(mEventID, title, location, time, price, thumbnailURL, urlClick, description, caveat, contestState, timeType);
        mEventID++;
        mDataProvider.add(event);
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress.getByName(Website.GOOGLE_URL);
            return true;

        } catch (UnknownHostException e) {
            Log.e(ERROR_TAG, "" + e.getMessage());
            Log.e(ERROR_TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    public void showNetworkError(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewPager.setVisibility(View.INVISIBLE);
                mTabHost.setVisibility((View.INVISIBLE));
                mNetWorkErrorView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showDataError(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewPager.setVisibility(View.INVISIBLE);
                mTabHost.setVisibility((View.INVISIBLE));
                mDataErrorView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showListProgressWheel(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.setVisibility(View.INVISIBLE);
                mEventListFragment.getProgressWheel().setVisibility(View.VISIBLE);
            }
        });
    }
    public void hideListProgressWheel(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEventListFragment.getProgressWheel().setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showMapProgressWheel(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGoogleMapFragment.getProgressWheel().setVisibility(View.VISIBLE);
            }
        });
    }
    public void hideMapProgressWheel(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGoogleMapFragment.getProgressWheel().setVisibility(View.INVISIBLE);
            }
        });
    }

    public void hideAllErrors(){
        ((Activity)mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if (mNetWorkErrorView.isShown())
                mNetWorkErrorView.setVisibility(View.INVISIBLE);
            else if (mDataErrorView.isShown())
                mDataErrorView.setVisibility(View.INVISIBLE);
            }
        });
    }

    //interface is in this class because only this class needs it.
    public interface NetworkProgressCommunicator {
        public LinearLayout getNetworkErrorView();
        public LinearLayout getDataErrorView();
        public ViewPager getViewPager();
        public MaterialTabHost getTabHost();
        public DataProvider getDataProvider();
        public SQLiteDB getDB();
        public String getFragmentTag(FragmentType fragmentType);
        public void setDataErrorMessage(String errorMessage);
    }
}
