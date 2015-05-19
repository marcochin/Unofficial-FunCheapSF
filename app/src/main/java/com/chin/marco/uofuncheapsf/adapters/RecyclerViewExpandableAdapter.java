package com.chin.marco.uofuncheapsf.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chin.marco.uofuncheapsf.R;
import com.chin.marco.uofuncheapsf.constants.Website;
import com.chin.marco.uofuncheapsf.pojo.Event;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.nio.charset.Charset;
import java.util.List;

import it.neokree.materialtabs.MaterialTabHost;

/**
 * Created by Marco on 3/18/2015.
 */
public class RecyclerViewExpandableAdapter extends AbstractExpandableItemAdapter<RecyclerViewExpandableAdapter.MyGroupViewHolder, RecyclerViewExpandableAdapter.MyChildViewHolder>
{
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private Context mContext;
    private List<Event> mEventList;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView mRecyclerView;
    private NoEventsCommunicator mNoEventsCommunicator;
    private ThumbnailClickListener mThumbnailClickListener;

    private boolean isPicClick;

    public RecyclerViewExpandableAdapter(Context context, NoEventsCommunicator noEventsCommunicator, List<Event> eventList,
                                         RecyclerViewExpandableItemManager recyclerViewExpandableItemManager,
                                         RecyclerView recyclerView){
        mContext = context;
        mEventList = eventList;
        mRecyclerViewExpandableItemManager = recyclerViewExpandableItemManager;
        mRecyclerView = recyclerView;
        mNoEventsCommunicator = noEventsCommunicator;
        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    public void setEventList(List<Event> eventList){
        mEventList = eventList;
        notifyDataSetChanged();

        TextView noEventsMessage = mNoEventsCommunicator.getNoEventsMsg();
        ViewPager viewPager = mNoEventsCommunicator.getViewPager();
        MaterialTabHost tabHost = mNoEventsCommunicator.getTabHost();

        if(mEventList.isEmpty()){
            tabHost.setVisibility(View.INVISIBLE);
            viewPager.setVisibility(View.INVISIBLE);
            noEventsMessage.setVisibility(View.VISIBLE);
        } else{
            if(noEventsMessage.getVisibility() == View.VISIBLE) {
                noEventsMessage.setVisibility(View.INVISIBLE);
            }

            if (viewPager.getVisibility() == View.INVISIBLE) {
                viewPager.setVisibility(View.VISIBLE);
                tabHost.setVisibility((View.VISIBLE));
            }
        }
    }

    public void setOnThumbnailClickListener(ThumbnailClickListener tcl){
        mThumbnailClickListener = tcl;
    }

    @Override
    public int getGroupCount() {
        return mEventList.size();
    }

    //There is only 1 child per event
    @Override
    public int getChildCount(int groupPosition) {
        return 1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mEventList.get(groupPosition).getEventID();
    }

    //Child id is unique per group, since only have 1 child id, it is always 0
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public RecyclerViewExpandableAdapter.MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.event_item, parent, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public RecyclerViewExpandableAdapter.MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.child_item, parent, false);
        return new MyChildViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(final RecyclerViewExpandableAdapter.MyGroupViewHolder holder, final int groupPosition, int viewType) {
        final Event event = mEventList.get(groupPosition);

        //holder.mTitle.setText(event.getTitle());
        holder.mTitle.setText(event.getTitle());
        holder.mLocation.setText(event.getLocation());
        holder.mPrice.setText(event.getPrice());
        holder.mTime.setText(event.getTime());

        //TODO error image and placeholder image?
        Picasso.with(mContext)
                .load(event.getThumbnailURL())
                .error(ContextCompat.getDrawable(mContext, R.drawable.on_error_thumbnail))
                .into(holder.mThumbnail, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //convert image url to UTF-8
                        byte ptext[] = event.getThumbnailURL().getBytes(UTF_8);
                        String utf8 = new String(ptext, ISO_8859_1);
                        Picasso.with(mContext)
                                .load(utf8)
                                .into(holder.mThumbnail);

                        //set back the more picasso friendly url string
                        event.setThumbnailURL(utf8);
                    }
                });

        holder.mThumbnail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                int eventID = motionEvent.getAction();

                switch (eventID){
                    case MotionEvent.ACTION_DOWN:
                        isPicClick = true;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        isPicClick = false;
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBindChildViewHolder(RecyclerViewExpandableAdapter.MyChildViewHolder holder, final int groupPosition, int childPosition, int viewType) {
        Event.Child child = mEventList.get(groupPosition).getChild();
        holder.mDescription.setText(child.getDescription());

        if(child.getCaveat().isEmpty()){
            holder.mCaveat.setVisibility(View.GONE);
        } else{
            if(!holder.mCaveat.isShown())
                holder.mCaveat.setVisibility(View.VISIBLE);
            holder.mCaveat.setText(child.getCaveat());
        }

        //Set contest Text to red if ended
        //else set to green if ongoing
        if(child.getContestState().isEmpty()){
            holder.mContestState.setVisibility(View.GONE);
        } else{
            if(!holder.mContestState.isShown())
                holder.mContestState.setVisibility(View.VISIBLE);

            if(child.getContestState().contains(Website.CONTEST_ENTER))
                holder.mContestState.setTextColor(Color.GREEN);
            else
                holder.mContestState.setTextColor(Color.RED);

            holder.mContestState.setText(child.getContestState());
        }

        //Add padding to description if caveat or contest text is shown. Just for styling purposes.
        if(holder.mCaveat.getVisibility() == View.GONE && holder.mContestState.getVisibility() == View.GONE)
            holder.mDescription.setPadding(0,0,0,0);
        else {
            //float paddingTop = DpPxConversionUtil.convertDpToPixel(5f, mContext);
            //getDimension automatically converts your dp to pixels i think
            holder.mDescription.setPadding(0, (int)mContext.getResources().getDimension(R.dimen.child_description_top_padding), 0, 0);
        }

        //Add listener to close child when child is clicked
        holder.mChildContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerViewExpandableItemManager.collapseGroup(groupPosition);
            }
        });
    }

    //A gotcha: onBind gets called after each expand and de-expand
    @Override
    public boolean onCheckCanExpandOrCollapseGroup(final RecyclerViewExpandableAdapter.MyGroupViewHolder holder, final int groupPosition, int x, int y, boolean expand) {
        if(isPicClick){
            Event event = mEventList.get(groupPosition);
            mThumbnailClickListener.onThumbnailClick(event.getThumbnailURL());
            isPicClick = false;
            return false;
        }

        holder.mContainer.setBackgroundResource(R.color.row_pressed);
        holder.mContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                holder.mContainer.setBackgroundResource(R.drawable.recyclerview_row);
            }
        }, 100);

        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        if(expand) {
            if(layoutManager.getPosition(holder.itemView) == layoutManager.findFirstVisibleItemPosition())
                holder.mContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getChildPosition(holder.itemView));
                    }
                });

            else {
                holder.mContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getChildPosition(holder.itemView) + 1);
                    }
                });
            }
        }

        return true;
    }

    class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        ViewGroup mContainer;
        TextView mTitle;
        TextView mLocation;
        TextView mTime;
        TextView mPrice;
        ImageView mThumbnail;

        MyGroupViewHolder(View v) {
            super(v);
            mContainer = (ViewGroup) v.findViewById(R.id.event_container);
            mTitle = (TextView) v.findViewById(R.id.event_title);
            mLocation = (TextView) v.findViewById(R.id.event_location);
            mTime = (TextView) v.findViewById(R.id.event_time);
            mPrice = (TextView) v.findViewById(R.id.event_price);
            mThumbnail = (ImageView) v.findViewById(R.id.event_thumbnail_image);
        }
    }

    class MyChildViewHolder extends AbstractExpandableItemViewHolder{
        ViewGroup mChildContainer;
        TextView mDescription;
        TextView mCaveat;
        TextView mContestState;
        Button mDetails;
        Button mMap;

        MyChildViewHolder(View v) {
            super(v);
            mChildContainer = (ViewGroup) v.findViewById(R.id.event_child_container);
            mDescription = (TextView)v.findViewById(R.id.event_description);
            mCaveat = (TextView)v.findViewById(R.id.event_caveat);
            mContestState = (TextView)v.findViewById(R.id.event_contest_state);
        }
    }

    public interface NoEventsCommunicator{
        public ViewPager getViewPager();
        public MaterialTabHost getTabHost();
        public TextView getNoEventsMsg();
    }

    public interface ThumbnailClickListener{
        public void onThumbnailClick(String img);
    }
}