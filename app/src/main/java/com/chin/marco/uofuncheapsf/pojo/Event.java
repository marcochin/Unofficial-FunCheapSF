package com.chin.marco.uofuncheapsf.pojo;

/**
 * Created by Marco on 3/3/2015.
 */
public class Event {
    public static final int NO_TIME = -1;
    public static final int MORNING = 0;
    public static final int AFTERNOON = 1;
    public static final int EVENING = 2;
    public static final int ALLDAY = 3;
    public static final int EVERY_SOMEDAY = 4;

    private int mEventID;

    private String mTitle;
    private String mTime;
    private String mLocation;
    private String mPrice;
    private String mThumbnailURL;
    private int mTimeType;
    private Child mChild;

    public Event(int eventID, String title, String location, String time, String price, String thumbnailURL,
                 String urlClick, String description, String caveat, String contestState, int timeType) {
        mEventID = eventID;
        mThumbnailURL = thumbnailURL;
        mTitle = title;
        mTime = time;
        mLocation = location;
        mPrice = price;

        if(timeType < -1 || timeType > 4 )
            throw new IllegalArgumentException("timeType must be between [-1,4]");
        else
            mTimeType = timeType;

        mChild = new Child(urlClick, description, caveat, contestState);
    }

    public int getEventID() {return mEventID; }
    public String getTitle() { return mTitle; }
    public String getTime() { return mTime; }
    public String getLocation() { return mLocation; }
    public String getPrice() { return mPrice; }
    public String getThumbnailURL() { return mThumbnailURL; }
    public int getTimeType(){ return mTimeType; }

    public void setThumbnailURL(String thumbnailURL){mThumbnailURL = thumbnailURL;}

    //getter for child
    public Child getChild() { return mChild; }

    public class Child {
        private String mURLClick;
        private String mDescription;
        private String mCaveat;
        private String mContestState;

        private Child(String urlClick, String description, String caveat, String contestState){
            mURLClick = urlClick;
            mDescription = description;
            mCaveat = caveat;
            mContestState = contestState;
        }

        //only getter methods
        public String getURLClick() { return mURLClick; }
        public String getDescription(){ return mDescription; }
        public String getCaveat(){ return mCaveat;}
        public String getContestState(){ return mContestState;}
    }
}
