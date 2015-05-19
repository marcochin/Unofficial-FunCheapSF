package com.chin.marco.uofuncheapsf.utils;

import android.util.Log;

import com.chin.marco.uofuncheapsf.pojo.Event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marco on 3/14/2015.
 */
public class StringUtil {
    private static final String ERROR_TAG = "StringUtil";

    public static String removeSlashes(String stringWithSlashes){
        String[] dateSplit = stringWithSlashes.split("/");
        StringBuilder sb = new StringBuilder("");
        for (String string : dateSplit){
            sb.append(string);
        }

        return sb.toString();
    }

    public static String getMaxPages(String pageString){
        //"Page 1 of 10" would return "10"
        String[] pageSplit = pageString.split("\\s");
        return pageSplit[pageSplit.length-1];
    }

    public static String getTime(String timeString){
        //"Sunday, March 8 All Day |" would return "All Day"
        Pattern pattern = Pattern.compile("([0-9]{1,2}:[0-9]{2}\\s(PM|pm|AM|am))|All\\sDay|Every\\s");
        Matcher matcher = pattern.matcher(timeString);

        //group() returns the string the iterator is pointing to
        if(matcher.find())
            return matcher.group();

        return "";
    }

    public static String getPriceFromCost(String costString){
        //"Cost: $5" would return "$5"
        String[] costSplit = costString.split("\\s");

        if(costSplit.length>1)
            return costSplit[1];
        else
            return  "";
    }

    public static int getTimeType(String time){
        if(time.isEmpty())
            return Event.NO_TIME;

        //must check isEvery and isAllDay first or it will crash!!
        if(isEverySomedayEvent(time))
            return Event.EVERY_SOMEDAY;
        else if(isAllDayEvent(time))
            return Event.ALLDAY;
        else if(isMorningEvent(time))
            return Event.MORNING;
        else if(isAfternoonEvent(time))
            return Event.AFTERNOON;
        else if(isEveningEvent(time))
            return Event.EVENING;

        return Event.NO_TIME;
    }

    private static boolean isEverySomedayEvent(String time){
        if(time.isEmpty())
            return false;

        if(time.contains("Every"))
            return true;

        return false;
    }

    private static boolean isAllDayEvent(String time){
        if(time.isEmpty())
            return false;

        if(time.equals("All Day"))
            return true;

        return false;
    }

    private static boolean isMorningEvent(String time){
        if(time.isEmpty())
            return false;

        String[] timeSplit = time.split("\\s|:");
        if(timeSplit.length>1){
            int hour = parseHour(timeSplit[0]);
            if (hour < 12 &&
                    (timeSplit[timeSplit.length - 1].equals("am") || timeSplit[timeSplit.length - 1].equals("AM")))
                return true;
        }
        return false;
    }

    private static boolean isAfternoonEvent(String time){
        if(time.isEmpty())
            return false;

        String[] timeSplit = time.split("\\s|:");
        if(timeSplit.length>1){
            int hour = parseHour(timeSplit[0]);
            if((hour == 12 || hour < 5) &&
                    (timeSplit[timeSplit.length-1].equals("pm") || timeSplit[timeSplit.length-1].equals("PM")))
                return true;
        }
        return false;
    }

    private static boolean isEveningEvent(String time){
        if(time.isEmpty())
            return false;

        String[] timeSplit = time.split("\\s|:");
        if(timeSplit.length>1){
            int hour = parseHour(timeSplit[0]);
            if(hour >= 5 && (timeSplit[timeSplit.length-1].equals("pm") || timeSplit[timeSplit.length-1].equals("PM")))
                return true;
        }
        return false;
    }

    private static int parseHour(String hourString) {
        try {
            return Integer.parseInt(hourString);
        } catch (NumberFormatException e) {
            Log.e(ERROR_TAG, "" + e.getMessage());
            Log.e(ERROR_TAG, Log.getStackTraceString(e));
            return -1;
        }
    }

    public static String replaceLast(String input, String regex, String replacement) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        int lastMatchStart = 0;
        do {
            lastMatchStart=matcher.start();
        } while (matcher.find());

        matcher.find(lastMatchStart);
        StringBuffer sb = new StringBuffer(input.length());

        // adds to the stringBuilder the input all the way until the part where the last match starts
        // and replaces the last match with the replacement
        //method says it will append the string between the previous and current match( matcher.find(lastMatchStart) ),
        // but i think since we used find(int), there is no memory of the previous match
        matcher.appendReplacement(sb, replacement);

        //add the rest of the input after the last match
        matcher.appendTail(sb);
        return sb.toString();
    }
}
