package com.chin.marco.uofuncheapsf.constants;

/**
 * Created by Marco on 3/4/2015.
 */
public class Website {
    public static final String FUN_CHEAP_SF_URL = "http://sf.funcheap.com/";
    public static final String PAGE_ENDPOINT = "/page/";
    public static final String GOOGLE_URL = "www.google.com"; //dont put http:// or any prefix
    public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static final String DATE_FORMAT = "yyyy/MM/dd";

    //selectors for the information i need
    public static final String EVENT_SELECTOR = ".tanbox.left";
    public static final String TITLE_SELECTOR = "span.title a"; //get ownText
    public static final String LOCATION_SELECTOR = "span.cost+span, a.tt+span";//get ownText
    public static final String TIME_SELECTOR = "div.meta.archive-meta"; //get ownText then split
    public static final String PRICE_SELECTOR = "span.cost, a.tt"; //if elements == 2 then a.tt's ownText else split span.cost
    public static final String THUMBNAILURL_SELECTOR = "div.thumbnail-wrapper img[src]";//src attribute
    public static final String URLCLICK_SELECTOR = "span.title a[href]"; //get href attribute
    public static final String DESCRIPTION_SELECTOR = "p";//get ownText
    public static final String CAVEAT_SELECTOR = "div.tooltip div.middle"; //get ownText
    public static final String PAGES_SELECTOR = "span.pages";//get ownText then split
    public static final String CONTEST_ENTER_SELECTOR = "span.badge-enter-contest";//get ownText
    public static final String CONTEST_ENDED_SELECTOR = "span.badge-contest-ended";//get ownText


    public static final String SRC_ATTRIBUTE = "src";
    public static final String HREF_ATTRIBUTE = "href";
    public static final String CONTEST_ENTER = "Win";
    public static final String CONTEST_ENDED = "Ended";
}
