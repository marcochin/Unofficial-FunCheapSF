package com.chin.marco.uofuncheapsf.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chin.marco.uofuncheapsf.R;
import com.chin.marco.uofuncheapsf.data.MenuStateProvider;
import com.chin.marco.uofuncheapsf.interfaces.FragmentCommunicator;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;

/**
 * Created by Marco on 3/24/2015.
 */
public class CalendarMenuDialog extends DialogFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener{
    private static final int SF_FUNCHEAP_START_YEAR = 2003;
    private static final int YEAR_RANGE = 25;

    private ImageView[] mBulletPoints;
    private boolean[] mBulletState;
    private FragmentCommunicator mActivity;
    private Calendar mUserCustomDate;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentCommunicator) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.calendar_menu, container, false);

        RelativeLayout rlToday = (RelativeLayout) v.findViewById(R.id.action_today);
        RelativeLayout rlTomorrow = (RelativeLayout) v.findViewById(R.id.action_tomorrow);
        RelativeLayout rlPickADate = (RelativeLayout) v.findViewById(R.id.action_pick_a_date);

        rlToday.setOnClickListener(this);
        rlTomorrow.setOnClickListener(this);
        rlPickADate.setOnClickListener(this);

        ImageView ivToday = (ImageView) v.findViewById(R.id.today_bulletpoint);
        ImageView ivTomorrow = (ImageView) v.findViewById(R.id.tomorrow_bulletpoint);
        ImageView ivPickADate = (ImageView) v.findViewById(R.id.pick_a_date_bulletpoint);

        mBulletPoints = new ImageView[] {ivToday, ivTomorrow, ivPickADate};
        mBulletState = MenuStateProvider.getInstance().getCalendarMenuBulletedState();

        mUserCustomDate = mActivity.getUserCustomDate();
        //remembering where all the bulletPoints were

        for(int i = 0; i < mBulletState.length; i++){
            if(mBulletState[i]) {
                if(mBulletPoints[i].getVisibility() == View.INVISIBLE)
                    mBulletPoints[i].setVisibility(View.VISIBLE);
            }
            else if(mBulletPoints[i].getVisibility() == View.VISIBLE)
                mBulletPoints[i].setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Window window = getDialog().getWindow();
        window.setDimAmount(0.00f);
        window.setGravity(Gravity.TOP | Gravity.RIGHT);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        //convert ImageView array to boolean array to save more memory
        for(int i = 0; i < mBulletPoints.length; i++){
            if(mBulletPoints[i].getVisibility() == View.VISIBLE)
                mBulletState[i] = true;
            else
                mBulletState[i] = false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.action_today:
                showBulletPoint(mBulletPoints[0]);
                handleTodayTomorrowClick(getString(R.string.today));
                break;
            case R.id.action_tomorrow:
                showBulletPoint(mBulletPoints[1]);
                handleTodayTomorrowClick(getString(R.string.tomorrow));
                break;
            case R.id.action_pick_a_date:

                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, mUserCustomDate.get(Calendar.YEAR), mUserCustomDate.get(Calendar.MONTH), mUserCustomDate.get(Calendar.DAY_OF_MONTH), false);

                Calendar currentDate = Calendar.getInstance();

                //2003 is the year sfFunCheap started
                datePickerDialog.setYearRange(SF_FUNCHEAP_START_YEAR, currentDate.get(Calendar.YEAR) + YEAR_RANGE);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
                datePickerDialog.show(getFragmentManager(), getString(R.string.action_calendar));
                break;
        }
        dismiss();
    }

    public void handleTodayTomorrowClick(String stringDay){
        if(stringDay.equals(getString(R.string.today)) || stringDay.equals(getString(R.string.tomorrow))) {

            //retrieve the old date's values only before getting new instance
            //TODO Consider putting this in a method because onDateSet() uses it too
            int prevUserCustomDay = mUserCustomDate.get(Calendar.DAY_OF_MONTH);
            int prevUserCustomMonth = mUserCustomDate.get(Calendar.MONTH);
            int prevUserCustomYear = mUserCustomDate.get(Calendar.YEAR);

            //set userCustomDate to the most current instance today/tomorrow possible
            mUserCustomDate = Calendar.getInstance();
            if (stringDay.equals(getString(R.string.tomorrow))) {
                mUserCustomDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            mActivity.setUserCustomDate(mUserCustomDate);
            mActivity.updateToolbarDateDelegate();

            //If previousDate doesn't match the selectedDate
            /*NOTE: to be more clear, if title is says Tomorrow, and you select Today on calendar,
            but in that moment(11:59:59 am) Tomorrow becomes today, it will only update tomorrow title to today.
            It WON'T update fragment because the dates are the SAME*/
            if (prevUserCustomYear != mUserCustomDate.get(Calendar.YEAR)
                    || prevUserCustomMonth != mUserCustomDate.get(Calendar.MONTH)
                    || prevUserCustomDay != mUserCustomDate.get(Calendar.DAY_OF_MONTH)) {

                mActivity.updateFragmentDelegate();
            }
        }

    }

    private void showBulletPoint(ImageView bulletPoint) {
        for (ImageView bullet : mBulletPoints) {
            //if bullet is show and it's a diff bullet then the one passed in
            if (bullet.isShown() && !bulletPoint.equals(bullet))
                bullet.setVisibility(View.INVISIBLE);
        }

        if (!bulletPoint.isShown())
            bulletPoint.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        //retrieve the old date's values before setting new date
        int prevUserCustomDay = mUserCustomDate.get(Calendar.DAY_OF_MONTH);
        int prevUserCustomMonth = mUserCustomDate.get(Calendar.MONTH);
        int prevUserCustomYear = mUserCustomDate.get(Calendar.YEAR);

        mUserCustomDate.set(year, month, day);
        //an optimization here would be only updateToolbarDate() only if custom date has changed OR Today has changed. but im too lazy to be THAT efficient
        //to be more clear, if title is says Tomorrow, and you select Tomorrow on calendar, but in that moment Tomorrow becomes today, it will update tomorrow title to today.
        //but it wont update the fragment
        mActivity.updateToolbarDateDelegate();
        //only update fragment if custom date has changed
        if(prevUserCustomYear != year || prevUserCustomMonth != month || prevUserCustomDay != day)
            mActivity.updateFragmentDelegate();
    }
}
