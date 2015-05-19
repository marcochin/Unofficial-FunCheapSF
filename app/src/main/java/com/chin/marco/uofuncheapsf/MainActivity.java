package com.chin.marco.uofuncheapsf;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.chin.marco.uofuncheapsf.adapters.RecyclerViewExpandableAdapter;
import com.chin.marco.uofuncheapsf.constants.Website;
import com.chin.marco.uofuncheapsf.data.DataProvider;
import com.chin.marco.uofuncheapsf.data.MenuStateProvider;
import com.chin.marco.uofuncheapsf.data.SQLiteDB;
import com.chin.marco.uofuncheapsf.fragments.CalendarMenuDialog;
import com.chin.marco.uofuncheapsf.fragments.EventListFragment;
import com.chin.marco.uofuncheapsf.fragments.FragmentGeneral;
import com.chin.marco.uofuncheapsf.fragments.GoogleMapFragment;
import com.chin.marco.uofuncheapsf.fragments.TimeMenuDialog;
import com.chin.marco.uofuncheapsf.interfaces.FragmentCommunicator;
import com.chin.marco.uofuncheapsf.logging.L;
import com.chin.marco.uofuncheapsf.networking.EventFeedThread;
import com.chin.marco.uofuncheapsf.services.CountdownToRefreshService;
import com.chin.marco.uofuncheapsf.utils.StringUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

public class MainActivity extends AppCompatActivity
        implements FragmentCommunicator, EventFeedThread.NetworkProgressCommunicator, View.OnClickListener, RecyclerViewExpandableAdapter.NoEventsCommunicator{
    private static final String ERROR_TAG = "MainActivity";
    private static final String TIME_MENU_TAG = "timeMenuDialog";
    private static final String CALENDAR_MENU_TAG = "calendarMenuDialog";

    private static final String BUNDLE_DAY = "bundle_day";
    private static final String BUNDLE_MONTH = "bundle_month";
    private static final String BUNDLE_YEAR = "bundle_year";

    private static final String EMAIL_ADDRESS = "mco6055@gmail.com";
    private static final String EMAIL_SUBJECT = "Unofficial FunCheapSF Error Msg";
    private static final String EMAIL_TYPE = "plain/text";

    private String mErrorMessage = "";

    private int mCurrentTabPosition = 0;
    private GoogleMapFragment mMapFragment;
    private EventListFragment mListFragment;

    private Thread mCurrentThread;



    private Calendar mUserCustomDate;
    private TextView mToolbarTitle;
    private LinearLayout mNetworkErrorView;
    private LinearLayout mDataErrorView;
    private TextView mNoEventsMsg;
    private ViewPager mViewPager;
    private MaterialTabHost mTabHost;
    private FloatingActionsMenu mFloatingActionMenu;

    private DataProvider sDataProvider = DataProvider.getInstance();
    private SQLiteDB sDB = SQLiteDB.getInstance(this);
    private FragmentManager mFragmentManager = getFragmentManager();

    private SimpleDateFormat mMDFormat;
    private SimpleDateFormat mMDYFormat;

    private Intent mCountdownService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById (R.id.toolbar);
        mToolbarTitle = (TextView)findViewById(R.id.toolbarTitle);
        mNetworkErrorView = (LinearLayout) findViewById(R.id.networkErrorView);
        mDataErrorView = (LinearLayout) findViewById(R.id.dataErrorView);
        mNoEventsMsg = (TextView) findViewById(R.id.no_events_message);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTabHost = (MaterialTabHost) findViewById(R.id.materialTabHost);
        mFloatingActionMenu = (FloatingActionsMenu) findViewById(R.id.floating_action_menu);
        FloatingActionButton prevDayNav = (FloatingActionButton)findViewById(R.id.prev_day_navigation);
        FloatingActionButton nextDayNav = (FloatingActionButton)findViewById(R.id.next_day_navigation);
        Button tryAgainData = (Button) findViewById(R.id.tryAgainButtonData);
        Button tryAgainNetwork = (Button) findViewById(R.id.tryAgainButtonNetwork);
        Button notifyMe = (Button) findViewById(R.id.notifyMeButtonData);

        mMDFormat = new SimpleDateFormat(getString(R.string.MMMdd), Locale.US);
        mMDYFormat = new SimpleDateFormat(getString(R.string.MMMddyyyy), Locale.US);

        ViewPagerAdapter adapter = new ViewPagerAdapter(mFragmentManager, this);

        //set onClickListeners for Buttons
        prevDayNav.setOnClickListener(this);
        nextDayNav.setOnClickListener(this);
        tryAgainData.setOnClickListener(this);
        tryAgainNetwork.setOnClickListener(this);
        notifyMe.setOnClickListener(this);

        //Setup ViewPager
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mTabHost.setSelectedNavigationItem(position);

                switch (position){
                    case 1:
                        mMapFragment.panToSF();
                        mMapFragment.enableMyLocation(true);
                        break;
                    case 0:
                    case 2:
                        mMapFragment.enableMyLocation(false);
                        break;
                }

            }
        });

        //Toolbar will now take on default Action Bar characteristics
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(savedInstanceState == null) {
            //Default user's custom date to Today's date
            mUserCustomDate = Calendar.getInstance();
        }
        else{
            //Save user's custom date on rotation, this is to avoid serialization
            int savedDay = savedInstanceState.getInt(BUNDLE_DAY);
            int savedMonth = savedInstanceState.getInt(BUNDLE_MONTH);
            int savedYear = savedInstanceState.getInt(BUNDLE_YEAR);

            mUserCustomDate = Calendar.getInstance();
            mUserCustomDate.set(savedYear, savedMonth, savedDay);
        }

        //Setup Material Tabs
        for (int i = 0; i < 3; i++){
            mTabHost.addTab(mTabHost.newTab()
                    .setIcon(adapter.getIcon(i))
                    .setTabListener(new MaterialTabListener() {
                @Override
                public void onTabSelected(MaterialTab materialTab) {
                    //mCurrentTabPosition = materialTab.getPosition();
                    mViewPager.setCurrentItem(materialTab.getPosition());
                }

                @Override
                public void onTabReselected(MaterialTab materialTab) {
                    switch (materialTab.getPosition()){
                        case 1:
                            mMapFragment.panToSF();
                            break;
                    }
                }

                @Override
                public void onTabUnselected(MaterialTab materialTab) {
                }
            }));
        }

        //animate title
        YoYo.with(Techniques.Tada)
                .delay(200)
                .duration(2000)
                .playOn(mToolbarTitle);

        mToolbarTitle.setText(getString(R.string.app_name));

        Thread appNameToDateTitle = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2700);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateToolbarDate();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        appNameToDateTitle.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BUNDLE_DAY, mUserCustomDate.get(Calendar.DAY_OF_MONTH));
        outState.putInt(BUNDLE_MONTH, mUserCustomDate.get(Calendar.MONTH));
        outState.putInt(BUNDLE_YEAR, mUserCustomDate.get(Calendar.YEAR));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.action_time:
                TimeMenuDialog timeMenuDialog = (TimeMenuDialog) mFragmentManager.findFragmentByTag(TIME_MENU_TAG);
                if(timeMenuDialog == null)
                    timeMenuDialog = new TimeMenuDialog();

                timeMenuDialog.show(mFragmentManager, TIME_MENU_TAG); //adds fragment to the manager
                return true;

            case R.id.action_calendar:
                CalendarMenuDialog calendarMenuDialog = (CalendarMenuDialog) mFragmentManager.findFragmentByTag(CALENDAR_MENU_TAG);
                if(calendarMenuDialog == null)
                    calendarMenuDialog = new CalendarMenuDialog();

                calendarMenuDialog.show(mFragmentManager, CALENDAR_MENU_TAG); //adds fragment to the manager
                return true;

            case R.id.action_popular:
                //TODO implement Upcoming Popular
                return true;
        }

        return false;
    }

    @Override
    protected void onStop() {
        mCountdownService = new Intent(this, CountdownToRefreshService.class);
        startService(mCountdownService);

        if(mCurrentThread != null)
            mCurrentThread.interrupt();

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!isMyServiceRunning(CountdownToRefreshService.class))
            sDB.deleteAllTables();
        else
            stopService(mCountdownService);

        updateFragment();
    }

    @Override
    protected void onDestroy() {
        sDB.deleteAllTables();
        stopService(mCountdownService);

        if(sDB!=null)
            sDB.close();

        super.onDestroy();
    }

    //this onClick is for the Navigation left and right buttons in the FAB!
    //onClicks for Try Again and Notify Me are in the xml
    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        switch(viewID){
            case R.id.prev_day_navigation:
                if(mUserCustomDate == null) return;
                mUserCustomDate.add(Calendar.DAY_OF_MONTH, -1);
                updateToolbarDate();
                updateFragment();
                break;
            case R.id.next_day_navigation:
                if(mUserCustomDate == null) return;
                mUserCustomDate.add(Calendar.DAY_OF_MONTH, 1);
                updateToolbarDate();
                updateFragment();
                break;
            case R.id.notifyMeButtonData:
                //need try catch block because device may not have an app to handle the intent
                try {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{EMAIL_ADDRESS});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
                    emailIntent.setType(EMAIL_TYPE);
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mErrorMessage + "\n\n" + getString(R.string.email_message));
                    startActivity(emailIntent);
                } catch(ActivityNotFoundException e){
                    L.t(this, getString(R.string.email_error));
                    Log.e(ERROR_TAG, e.getMessage());
                    Log.e(ERROR_TAG, Log.getStackTraceString(e));
                }
                break;
            case R.id.tryAgainButtonData:
            case R.id.tryAgainButtonNetwork:
                updateFragment();
                break;
        }
    }

    public void updateToolbarDate() {
        //get a newInstance every update because a person can be using app at midnight and Today actually becomes tomorrow
        Calendar mToday = Calendar.getInstance();
        Calendar mTomorrow = Calendar.getInstance();
        mTomorrow.add(Calendar.DAY_OF_MONTH, 1);

        int todaysDay = mToday.get(Calendar.DAY_OF_MONTH);
        int todaysMonth = mToday.get(Calendar.MONTH);
        int todaysYear = mToday.get(Calendar.YEAR);

        int tomorrowsDay = mTomorrow.get(Calendar.DAY_OF_MONTH);
        int tomorrowsMonth = mTomorrow.get(Calendar.MONTH);
        int tomorrowsYear = mTomorrow.get(Calendar.YEAR);

        int userCustomDay = mUserCustomDate.get(Calendar.DAY_OF_MONTH);
        int userCustomMonth = mUserCustomDate.get(Calendar.MONTH);
        int userCustomYear = mUserCustomDate.get(Calendar.YEAR);

        //Find out what day of the week it is
        int userCustomDayOfWeek = mUserCustomDate.get(Calendar.DAY_OF_WEEK);
        String dayOfWeek = getDayOfWeek(userCustomDayOfWeek);

        //check if it's today
        if (todaysYear == userCustomYear && todaysMonth == userCustomMonth && todaysDay == userCustomDay) {
            setToolBarTitle(getString(R.string.today), mMDFormat);
            MenuStateProvider.getInstance().showCalendarBullet(MenuStateProvider.CalendarState.TODAY);
        }
        //check if it's tomorrow
        else if (tomorrowsYear == userCustomYear && tomorrowsMonth == userCustomMonth && tomorrowsDay == userCustomDay) {
            setToolBarTitle(getString(R.string.tomorrow), mMDFormat);
            MenuStateProvider.getInstance().showCalendarBullet(MenuStateProvider.CalendarState.TOMORROW);
        }

        //if he picks a date in a different year, show the year as well
        else if (todaysYear != userCustomYear){
            setToolBarTitle(dayOfWeek, mMDYFormat);
            MenuStateProvider.getInstance().showCalendarBullet(MenuStateProvider.CalendarState.PICK_A_DATE);
        }
        //else just display the normal date
        else {
            setToolBarTitle(dayOfWeek, mMDFormat);
            MenuStateProvider.getInstance().showCalendarBullet(MenuStateProvider.CalendarState.PICK_A_DATE);
        }
    }

    public void updateFragment(){

        /*TODO: Instead of updating ListFragment it should update the current fragment.
          TODO: The other fragments should update to the correct date if you tab to them
          TODO: dont update all 3 fragments at once*/

        SimpleDateFormat mdySlashFormat = new SimpleDateFormat(Website.DATE_FORMAT, Locale.US);

        String dateEndPoint = mdySlashFormat.format(mUserCustomDate.getTime());
        String dateEndPointNoSlashes = StringUtil.removeSlashes(dateEndPoint);

        //TODO should i make a helper class to for network operation? YES!
        //Start new thread for online task or read from database if table exists
        if(!sDB.isTableExist(dateEndPointNoSlashes)) {
            if(mCurrentThread != null)
                mCurrentThread.interrupt();

            mCurrentThread = new Thread(new EventFeedThread(this, mFragmentManager, dateEndPoint));
            mCurrentThread.start();
        } else{
            //database tables cant have slashes
            //TODO when filtering using the time menu you don't have to load from db, just need to update so put a if statement here..
            sDB.loadEventListFromTable(dateEndPointNoSlashes, sDataProvider);
            //TODO update all 3?
            mListFragment.update();
        }
    }

    @Override
    public void updateFragmentDelegate() {
        updateFragment();
    }

    @Override
    public void updateToolbarDateDelegate() {
        updateToolbarDate();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setToolBarTitle(String dayOfWeek, SimpleDateFormat sdf){ mToolbarTitle.setText(dayOfWeek + " " + sdf.format(mUserCustomDate.getTime())); }

    public String getDayOfWeek(int dayOfWeekNum){
        String dayOfWeek = "";

        switch(dayOfWeekNum){
            case 1:
                dayOfWeek = getString(R.string.sunday);
                break;
            case 2:
                dayOfWeek = getString(R.string.monday);
                break;
            case 3:
                dayOfWeek = getString(R.string.tuesday);
                break;
            case 4:
                dayOfWeek = getString(R.string.wednesday);
                break;
            case 5:
                dayOfWeek = getString(R.string.thursday);
                break;
            case 6:
                dayOfWeek = getString(R.string.friday);
                break;
            case 7:
                dayOfWeek = getString(R.string.saturday);
                break;
        }

        return dayOfWeek;
    }

    @Override
    public int getCurrentTabPosition(){return mCurrentTabPosition;}

    @Override
    public Calendar getUserCustomDate() { return mUserCustomDate; }

    @Override
    public void setUserCustomDate(Calendar userCustomDate) {mUserCustomDate = userCustomDate;}

    @Override
    public DataProvider getDataProvider() { return sDataProvider; }

    @Override
    public FloatingActionsMenu getFloatingActionMenu() {return mFloatingActionMenu;}

    @Override
    public SQLiteDB getDB() { return sDB; }

    @Override
    public String getFragmentTag(EventFeedThread.FragmentType fragmentType) {
        switch(fragmentType){
            case EVENT_LIST_FRAGMENT:
                return mListFragment.getTag();
            case GOOGLE_MAP_FRAGMENT:
                return mMapFragment.getTag();
        }
        return "";
    }

    @Override
    public void setDataErrorMessage(String errorMessage) {mErrorMessage = errorMessage;}

    @Override
    public LinearLayout getNetworkErrorView() { return mNetworkErrorView; }

    @Override
    public LinearLayout getDataErrorView() { return mDataErrorView; }

    @Override
    public ViewPager getViewPager() { return mViewPager; }

    @Override
    public TextView getNoEventsMsg() { return mNoEventsMsg; }

    @Override
    public MaterialTabHost getTabHost() { return mTabHost; }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        //icons corresponding to each tab
        private int mIcons[] = {R.drawable.ic_action_list, R.drawable.ic_action_map, R.drawable.ic_action_two_fingers};
        private Context mContext;

        public ViewPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position){
                case 0 :
                    fragment = new EventListFragment();
                    mListFragment = (EventListFragment) fragment;
                    break;
                case 1:
                    fragment = new GoogleMapFragment();
                    mMapFragment = (GoogleMapFragment) fragment;
                    break;
                case 2:
                    fragment = FragmentGeneral.getInstance(position);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            //return 3 for 3 tabs. adjust if you add or take away tabs
            return 3;
        }

        public Drawable getIcon(int position){
            return ContextCompat.getDrawable(mContext, mIcons[position]);
        }
    }
}
