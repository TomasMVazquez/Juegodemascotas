package com.applications.toms.juegodemascotas.view.menu_fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applications.toms.juegodemascotas.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaysToGoFragment extends Fragment {

    public static final String TAG = "PlaysToGoFragment";



    public PlaysToGoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plays_to_go, container, false);



        return view;
    }

}
