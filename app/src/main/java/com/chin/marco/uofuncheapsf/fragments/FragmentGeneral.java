package com.chin.marco.uofuncheapsf.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chin.marco.uofuncheapsf.R;

/**
 * Created by Marco on 3/2/2015.
 */
public class FragmentGeneral extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general, container, false);
        TextView tv = (TextView) view.findViewById(R.id.textView);
        int pos = getArguments().getInt("page");
        tv.setText("This is page " + pos);
        return view;
    }

    public static Fragment getInstance(int pos){
        FragmentGeneral frag = new FragmentGeneral();
        Bundle bundle = new Bundle();
        bundle.putInt("page", pos);
        frag.setArguments(bundle);
        return frag;
    }
}
