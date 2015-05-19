package com.chin.marco.uofuncheapsf.data;

import com.chin.marco.uofuncheapsf.pojo.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco on 3/20/2015.
 */
public class DataProvider {
    private static DataProvider sDataProvider;
    private List<Event> mFilteredEventList = new ArrayList<>();
    private List<Event> mMorningList = new ArrayList<>();
    private List<Event> mAfternoonList = new ArrayList<>();
    private List<Event> mEveningList = new ArrayList<>();
    private List<Event> mAllDayList = new ArrayList<>();
    //private List<Event> mEverySomedayList = new ArrayList<>();

    private DataProvider(){};

    public static DataProvider getInstance(){
        if(sDataProvider == null)
            sDataProvider = new DataProvider();

        return sDataProvider;
    }

    public void clear(){
        mFilteredEventList.clear();
        mAllDayList.clear();
        mMorningList.clear();
        mAfternoonList.clear();
        mEveningList.clear();
        //mEverySomedayList.clear();
    }


    public void add(Event event){
        int timeType = event.getTimeType();

        switch(timeType){
            case Event.ALLDAY:
                mAllDayList.add(event);
                break;
            case Event.MORNING:
                mMorningList.add(event);
                break;
            case Event.AFTERNOON:
                mAfternoonList.add(event);
                break;
            case Event.EVENING:
                mEveningList.add(event);
                break;
            /*case Event.EVERY_SOMEDAY:
                mEverySomedayList.add(event);
                break;*/
        }
    }

    //this is basically the no filtered list for now..
    //The order in which events are shown
    //TODO should return a single list based on the filter because adapter can only accept one list
    public List<Event> getFullEventList() {
        List<Event> eventList = new ArrayList<>();

        for (Event event : mAllDayList)
            eventList.add(event);

        for (Event event : mMorningList)
            eventList.add(event);

        for (Event event : mAfternoonList)
            eventList.add(event);

        for (Event event : mEveningList)
            eventList.add(event);

        /*for(Event event: mEverySomedayList)
            mFilteredEventList.add(event);*/

        return eventList;
    }

    public synchronized List<Event> getFilteredEventList() {
        mFilteredEventList.clear();

        boolean[] timeMenuCheckedState = MenuStateProvider.getInstance().getTimeMenuCheckedState();

        //If No Filter is checked or itself it checked
        if(timeMenuCheckedState[0] || timeMenuCheckedState[1]) {
            for (Event event : mAllDayList)
                mFilteredEventList.add(event);
        }

        if(timeMenuCheckedState[0] || timeMenuCheckedState[2]) {
            for (Event event : mMorningList)
                mFilteredEventList.add(event);
        }

        if(timeMenuCheckedState[0] || timeMenuCheckedState[3]) {
            for (Event event : mAfternoonList)
                mFilteredEventList.add(event);
        }

        if(timeMenuCheckedState[0] || timeMenuCheckedState[4]) {
            for (Event event : mEveningList)
                mFilteredEventList.add(event);
        }

        /*for(Event event: mEverySomedayList)
            mFilteredEventList.add(event);*/

        return mFilteredEventList;
    }

    public List<Event> getMorningList() { return mMorningList; }

    public List<Event> getAfternoonList() { return mAfternoonList; }

    public List<Event> getEveningList() { return mEveningList; }

    public List<Event> getAllDayList() { return mAllDayList; }

    //public List<Event> getEverySomedayList() { return mEverySomedayList; }
}
