package com.chin.marco.uofuncheapsf.data;

/**
 * Created by Marco on 3/25/2015.
 */
public class MenuStateProvider {
    public enum CalendarState{
        TODAY, TOMORROW, PICK_A_DATE
    }

    private static MenuStateProvider sMenuStateProvider;
    private boolean[] mTimeMenuCheckedState;
    private boolean[] mCalendarMenuBulletedState;

    private MenuStateProvider(){
        //each boolean corresponds to a menu row of that menu.
        mTimeMenuCheckedState = new boolean[] {true, false, false, false, false};
        mCalendarMenuBulletedState= new boolean[] {true, false, false};
    };

    public static MenuStateProvider getInstance(){
        if(sMenuStateProvider == null)
            sMenuStateProvider = new MenuStateProvider();

        return sMenuStateProvider;
    }

    public boolean[] getTimeMenuCheckedState() {
        return mTimeMenuCheckedState;
    }

    public boolean[] getCalendarMenuBulletedState() {
        return mCalendarMenuBulletedState;
    }

    public void showCalendarBullet(CalendarState calendarState){
        switch (calendarState){
            case TODAY:
                mCalendarMenuBulletedState = new boolean[]{true, false, false};
                break;
            case TOMORROW:
                mCalendarMenuBulletedState = new boolean[]{false, true, false};
                break;
            case PICK_A_DATE:
                mCalendarMenuBulletedState = new boolean[]{false, false, false};
                break;
        }
    }
}
