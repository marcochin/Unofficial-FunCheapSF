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

/**
 * Created by Marco on 3/23/2015.
 */
public class TimeMenuDialog extends DialogFragment implements View.OnClickListener{

    private ImageView[] mCheckMarks;
    private boolean[] mCheckedState;
    private FragmentCommunicator mActivity;

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

        View v = inflater.inflate(R.layout.time_menu, container, false);
        RelativeLayout rlNoFilter = (RelativeLayout) v.findViewById(R.id.action_no_filter);
        RelativeLayout rlAllDay = (RelativeLayout) v.findViewById(R.id.action_all_day);
        RelativeLayout rlMorning = (RelativeLayout) v.findViewById(R.id.action_morning);
        RelativeLayout rlAfternoon = (RelativeLayout) v.findViewById(R.id.action_afternoon);
        RelativeLayout rlEvening = (RelativeLayout) v.findViewById(R.id.action_evening);

        rlNoFilter.setOnClickListener(this);
        rlAllDay.setOnClickListener(this);
        rlMorning.setOnClickListener(this);
        rlAfternoon.setOnClickListener(this);
        rlEvening.setOnClickListener(this);


        ImageView ivNoFilter = (ImageView) v.findViewById(R.id.no_filter_checkmark);
        ImageView ivAllDay = (ImageView) v.findViewById(R.id.all_day_checkmark);
        ImageView ivMorning = (ImageView) v.findViewById(R.id.morning_checkmark);
        ImageView ivAfternoon = (ImageView) v.findViewById(R.id.afternoon_checkmark);
        ImageView ivEvening = (ImageView) v.findViewById(R.id.evening_checkmark);

        mCheckMarks = new ImageView[] {ivNoFilter, ivAllDay, ivMorning, ivAfternoon, ivEvening};
        mCheckedState = MenuStateProvider.getInstance().getTimeMenuCheckedState();

        //remembering where all the checkmarks were
        for(int i =0; i < mCheckedState.length; i++){
            if(mCheckedState[i]) {
                if((mCheckMarks[i].getVisibility() == View.INVISIBLE))
                    mCheckMarks[i].setVisibility(View.VISIBLE);
            }
            else if(mCheckMarks[i].getVisibility() == View.VISIBLE)
                mCheckMarks[i].setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Window window = getDialog().getWindow();
        window.setDimAmount(0.00f);
        window.setGravity(Gravity.TOP | Gravity.END);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        boolean previousCheckedState[] = new boolean[mCheckedState.length];

        //copy the previous state to a new array
        for(int i = 0; i < mCheckMarks.length; i++){
            previousCheckedState[i] = mCheckedState[i];
        }

        //convert ImageView array to boolean array to save more memory
        for(int i = 0; i < mCheckMarks.length; i++){
            if(mCheckMarks[i].getVisibility() == View.VISIBLE)
                mCheckedState[i] = true;
            else
                mCheckedState[i] = false;
        }

        //only update fragment if values are different else dont waste resources!
        for(int i = 0; i <  mCheckMarks.length; i++){
            if(previousCheckedState[i] != mCheckedState[i]) {
                mActivity.updateFragmentDelegate();
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.action_no_filter:
                showNoFilterCheckMark();
                break;
            case R.id.action_all_day:
                showCheckMark(mCheckMarks[1]);
                break;
            case R.id.action_morning:
                showCheckMark(mCheckMarks[2]);
                break;
            case R.id.action_afternoon:
                showCheckMark(mCheckMarks[3]);
                break;
            case R.id.action_evening:
                showCheckMark(mCheckMarks[4]);
                break;
        }
    }

    private void showCheckMark(ImageView checkMark){
        //if filter is selected, uncheck no filter
        if (mCheckMarks[0].isShown())
            mCheckMarks[0].setVisibility(View.INVISIBLE);

        //check or uncheck a filter
        if (checkMark.isShown())
            checkMark.setVisibility(View.INVISIBLE);
        else
            checkMark.setVisibility(View.VISIBLE);

        //if nothing is checked, default to checking no filter
        if(!mCheckMarks[1].isShown() && !mCheckMarks[2].isShown() && !mCheckMarks[3].isShown() && !mCheckMarks[4].isShown())
            mCheckMarks[0].setVisibility(View.VISIBLE);
        //if all is checked, default to checking no filter
        else if(mCheckMarks[1].isShown() && mCheckMarks[2].isShown() && mCheckMarks[3].isShown() && mCheckMarks[4].isShown())
            showNoFilterCheckMark();
    }

    private void showNoFilterCheckMark(){
        for(int i = 1; i < mCheckMarks.length; i++)
            if(mCheckMarks[i].isShown())
                mCheckMarks[i].setVisibility(View.INVISIBLE);

        mCheckMarks[0].setVisibility(View.VISIBLE);
    }
}
