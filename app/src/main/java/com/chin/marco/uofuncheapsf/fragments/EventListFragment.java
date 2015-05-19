package com.chin.marco.uofuncheapsf.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chin.marco.uofuncheapsf.ImageActivity;
import com.chin.marco.uofuncheapsf.R;
import com.chin.marco.uofuncheapsf.adapters.RecyclerViewExpandableAdapter;
import com.chin.marco.uofuncheapsf.customclasses.MyCustomLayoutManager;
import com.chin.marco.uofuncheapsf.data.DataProvider;
import com.chin.marco.uofuncheapsf.interfaces.FragmentCommunicator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.pnikosis.materialishprogress.ProgressWheel;


public class EventListFragment extends Fragment{
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private FragmentCommunicator mActivity;

    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableAdapter mAdapter;
    private ProgressWheel mProgressWheel;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentCommunicator)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getTag returns "android:switcher:2131296349:0"
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressWheel = (ProgressWheel) view.findViewById(R.id.progress_wheel);
        //get reference to recyclerView and set LayoutManager
        mRecyclerView = (RecyclerView) view.findViewById(R.id.event_list_recycler_view);
        mLayoutManager = new MyCustomLayoutManager(getActivity());

        //If a group is expanded, it will stay expanded on fragment recreation
        Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        //create adapters
        mAdapter = new RecyclerViewExpandableAdapter(getActivity(), (RecyclerViewExpandableAdapter.NoEventsCommunicator)getActivity(),
                DataProvider.getInstance().getFilteredEventList(), mRecyclerViewExpandableItemManager, mRecyclerView);
        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);// wrap for expanding

        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getActivity(), R.drawable.list_divider), true));
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        mAdapter.setOnThumbnailClickListener(new RecyclerViewExpandableAdapter.ThumbnailClickListener() {
            @Override
            public void onThumbnailClick(String img) {
                Intent imageActivity = new Intent(getActivity(), ImageActivity.class);
                imageActivity.putExtra(ImageActivity.IMAGE_ACTIVITY_TAG, img);
                startActivity(imageActivity);
            }
        });

        //Makes the FloatingActionMenu Invisible on Scroll
        //And also set booleans to record the group positions so we can calculate and split the top items from the bottom
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //gets called only when state changes not all the time, even though it says "or position"
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    mActivity.getFloatingActionMenu().setVisibility(View.INVISIBLE);
                else if(newState == RecyclerView.SCROLL_STATE_IDLE)
                    mActivity.getFloatingActionMenu().setVisibility(View.VISIBLE);
            }
        });
    }

    //called after onCreateView
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //update fragment when ui is recreated
        //update();
        //mActivity.updateFragmentDelegate();
    }

    public void update() {
        mAdapter.setEventList(DataProvider.getInstance().getFilteredEventList());
    }

    public RecyclerViewExpandableAdapter getRecyclerViewAdapter(){
        return mAdapter;
    }

    public RecyclerView getRecyclerView(){
        return mRecyclerView;
    }

    public ProgressWheel getProgressWheel(){
        return mProgressWheel;
    }

    @Override
    public void onPause() {
        Log.d("poo", "FragonPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("poo", "FragonStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("poo", "FragonDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.d("poo", "FragonDestroyView");
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }

        mLayoutManager = null;
        super.onDestroyView();
    }
}