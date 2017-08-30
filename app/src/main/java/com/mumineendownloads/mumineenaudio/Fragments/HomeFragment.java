package com.mumineendownloads.mumineenaudio.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Adapters.AudioAdapter;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.CustomDivider;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    private int position;
    private String album;


    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = 0;
        Bundle bundle = this.getArguments();

        if(bundle != null){
            Log.e("Bundle", String.valueOf(bundle));
            position = bundle.getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Fonty.setFonts((ViewGroup) rootView);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getItemAnimator().setChangeDuration(0);
        AudioDB db = new AudioDB(getContext());

        ArrayList<String> arrayList = db.getAlbums();
        for(int i =0; i<arrayList.size();i++){
            if(position==i){
                album=arrayList.get(i);
            }
        }
        ArrayList<Audio.AudioItem> audioItems = db.getAllPDFS(album);
        Collections.sort(audioItems, new Comparator<Audio.AudioItem>() {
            @Override
            public int compare(Audio.AudioItem o1, Audio.AudioItem o2) {
                return o1.getCat().compareTo(o2.getCat());
            }
        });
        recyclerView.addItemDecoration(new CustomDivider(getContext(),audioItems));
        recyclerView.setAdapter(new AudioAdapter(audioItems,getContext(),HomeFragment.this));

        return rootView;
    }

}
