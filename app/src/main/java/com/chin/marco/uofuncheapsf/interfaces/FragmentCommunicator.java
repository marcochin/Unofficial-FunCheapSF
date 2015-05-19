package com.chin.marco.uofuncheapsf.interfaces;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Calendar;

/**
 * Created by Marco on 3/3/2015.
 */
public interface FragmentCommunicator {
    public int getCurrentTabPosition();
    //public DataProvider getDataProvider();
    //public SQLiteDB getDB();
    public Calendar getUserCustomDate();
    public FloatingActionsMenu getFloatingActionMenu();
    public void setUserCustomDate(Calendar userCustomDate);
    public void updateFragmentDelegate();
    public void updateToolbarDateDelegate();
}
