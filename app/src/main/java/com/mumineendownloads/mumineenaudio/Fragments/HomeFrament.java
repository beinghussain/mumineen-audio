package com.mumineendownloads.mumineenaudio.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Activities.MainActivity;
import com.mumineendownloads.mumineenaudio.Adapters.AlbumAdapter;
import com.mumineendownloads.mumineenaudio.Adapters.AudioCardAdapter;
import com.mumineendownloads.mumineenaudio.Adapters.RecentAdapter;
import com.mumineendownloads.mumineenaudio.Adapters.SavedAdapter;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer;
import com.mumineendownloads.mumineenaudio.Helpers.CustomDivider;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.AlbumModel;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.Models.RecentlyPlayed;
import com.mumineendownloads.mumineenaudio.Models.SectionDataModel;
import com.mumineendownloads.mumineenaudio.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import es.dmoral.toasty.Toasty;

import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_AUDIO;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_PAUSE;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_PLAY;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_RESUME;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_STOP;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_BUFFERING;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_NULL;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PAUSED;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PLAYING;

public class HomeFrament extends Fragment {


    private ArrayList<AlbumModel> allAlbums = new ArrayList<>();
    private Audio.AudioItem playingAudio = new Audio.AudioItem();
    private String currentState = STATE_NULL;
    private RecentAdapter recentAdapter;
    private ArrayList<Audio.AudioItem> playlist;
    private DownloadReceiver mReceiver;
    RelativeLayout.LayoutParams params;
    public static ScrollView scrollView;
    private AudioCardAdapter playlistItemAdapter;
    private RecentAdapter mostAdapter;
    private ArrayList<RecentlyPlayed> queue;
    private ArrayList<RecentlyPlayed> mostPlayed;
    private RelativeLayout mostPlayedContainer, recentContainer, playlistContainer, savedContainer;
    private ArrayList<Audio.AudioItem> savedList;
    private SavedAdapter savedAdpater;

    public HomeFrament() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Fonty.setFonts(getActivity());

        View v =  inflater.inflate(R.layout.fragment_home, container, false);
        scrollView = v.findViewById(R.id.scrollView);
int pix = Utils.dpToPx(getContext());
        if(MainActivity.slidingPaneLayout.getPanelState()!= SlidingUpPanelLayout.PanelState.HIDDEN){
            params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0,0,0,pix);
            scrollView.setLayoutParams(params);
        }else {
            params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0,0,0,0);
            scrollView.setLayoutParams(params);
        }

        recentContainer = v.findViewById(R.id.recent_conainer);
        playlistContainer = v.findViewById(R.id.playlist_container);
        mostPlayedContainer = v.findViewById(R.id.most_container);
        savedContainer = v.findViewById(R.id.saved_container);

        RecyclerView albumListRecyclerView = v.findViewById(R.id.albums);
        ArrayList<String> arrayList = new AudioDB(getContext()).getAlbums();
        allAlbums = new ArrayList<>();
        for(int i=0; i<arrayList.size(); i++) {
            AlbumModel albumModel = new AlbumModel(arrayList.get(i), i, i*2);
            allAlbums.add(albumModel);
        }
        MainActivity.toolbar.setTitle("Home");
        AlbumAdapter albumAdapter = new AlbumAdapter(allAlbums,getActivity().getApplicationContext(),HomeFrament.this);
        albumListRecyclerView.setAdapter(albumAdapter);
        albumListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        albumListRecyclerView.addItemDecoration(new CustomDivider(getContext()));

        RecyclerView playListRecyclerView = v.findViewById(R.id.playlist);
        playlist = Utils.getPlaylist(getContext());
        if(playlist.size()==0){
           playlistContainer.setVisibility(View.GONE);
        }
        playlistItemAdapter = new AudioCardAdapter(playlist,getActivity().getApplicationContext(),HomeFrament.this,"playlist");
        playListRecyclerView.setAdapter(playlistItemAdapter);
        playListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        playListRecyclerView.addItemDecoration(new CustomDivider(getContext()));
        playListRecyclerView.getItemAnimator().setChangeDuration(0);

        RecyclerView recentRecyclerView= v.findViewById(R.id.queue);
        queue = Utils.getRecentList(getContext());
        if(queue.size()==0){
            recentContainer.setVisibility(View.GONE);
        }
        Collections.sort(queue, new Comparator<RecentlyPlayed>() {
            @Override
            public int compare(RecentlyPlayed o1, RecentlyPlayed o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });

        recentAdapter = new RecentAdapter(queue,getActivity().getApplicationContext(),HomeFrament.this,"recent");
        Utils.saveCurrentRecentList(queue,getContext());
        recentRecyclerView.setAdapter(recentAdapter);
        recentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        recentRecyclerView.addItemDecoration(new CustomDivider(getContext()));
        recentRecyclerView.getItemAnimator().setChangeDuration(0);


        RecyclerView mostlyPlayed= v.findViewById(R.id.most);
        mostPlayed = Utils.getRecentList(getContext());

        if(mostPlayed.size()==0){
            mostPlayedContainer.setVisibility(View.GONE);
        }
        Collections.sort(mostPlayed, new Comparator<RecentlyPlayed>() {
            @Override
            public int compare(RecentlyPlayed o1, RecentlyPlayed o2) {
                return o2.getCount() - (o1.getCount());
            }
        });
        Utils.saveCurrentMostList(queue,getContext());
        mostAdapter = new RecentAdapter(mostPlayed,getActivity().getApplicationContext(),HomeFrament.this,"most");
        mostlyPlayed.setAdapter(mostAdapter);
        mostlyPlayed.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        mostlyPlayed.addItemDecoration(new CustomDivider(getContext()));
        mostlyPlayed.getItemAnimator().setChangeDuration(0);

        RecyclerView saved= v.findViewById(R.id.saved);

        savedList = Utils.getSavedList(getContext());
        if(savedList.size()==0){
            savedContainer.setVisibility(View.GONE);
        }

        Collections.sort(savedList, new Comparator<Audio.AudioItem>() {
            @Override
            public int compare(Audio.AudioItem o1, Audio.AudioItem o2) {
                return o2.getAid() - (o1.getAid());
            }
        });

        savedAdpater = new SavedAdapter(savedList,getActivity().getApplicationContext(),HomeFrament.this,"saved");
        saved.setAdapter(savedAdpater);
        saved.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        saved.addItemDecoration(new CustomDivider(getContext()));
        saved.getItemAnimator().setChangeDuration(0);

        if(MainActivity.getPlayingAudio().getAid()!=-1){
            playingAudio = MainActivity.getPlayingAudio();
            currentState = MainActivity.getCurrentState();
            updateAdapter();
        }


        return v;
    }

    public void playPause() {
        if(currentState.equals(AudioPlayer.STATE_PAUSED)){
            AudioPlayer.actionIntent(getContext(),AudioPlayer.ACTION_RESUME);
        }
        else if(currentState.equals(STATE_PLAYING)) {
            AudioPlayer.actionIntent(getContext(), AudioPlayer.ACTION_PAUSE);
        }
    }

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                String state = intent.getStringExtra("state");
                String action = intent.getStringExtra("action");
                switch (action) {
                    case ACTION_AUDIO:
                        playingAudio = (Audio.AudioItem) intent.getSerializableExtra("audio");
                        currentState = STATE_BUFFERING;
                        updateAdapterMain();
                        break;
                    case ACTION_RESUME:
                        currentState = STATE_PLAYING;
                        updateAdapter();
                        break;
                    case ACTION_PLAY:
                        currentState = STATE_PLAYING;
                        updateAdapter();
                        break;
                    case ACTION_PAUSE:
                        currentState = STATE_PAUSED;
                        updateAdapter();
                        break;
                    case ACTION_STOP:
                        currentState = STATE_NULL;
                        updateAdapter();
                        break;
                }
            }
        }
    }

    public void updateAdapter() {
        int position1 = getIndexOf(playingAudio,playlist);
        playlistItemAdapter.notifyItemChanged(position1);
        int pos2 = getIndexOfRecentlyPlayed(playingAudio,queue);
        recentAdapter.notifyItemChanged(pos2);
        int pos3 = getIndexOfRecentlyPlayed(playingAudio,mostPlayed);
        mostAdapter.notifyItemChanged(pos3);
    }

    public void updateAdapterMain() {
        playlistItemAdapter.notifyDataSetChanged();
        recentAdapter.notifyDataSetChanged();
        mostAdapter.notifyDataSetChanged();
    }

    private void register() {
        mReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioPlayer.FILTER_AUDIO_DATA);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    private int getIndexOf(Audio.AudioItem playingAudio, ArrayList<Audio.AudioItem> arrayList) {
        for(int i = 0; i<arrayList.size(); i++){
            if(arrayList.get(i).getAid()==playingAudio.getAid()){
                return i;
            }
        } return -1;
    }

    private int getIndexOfRecentlyPlayed(Audio.AudioItem playingAudio, ArrayList<RecentlyPlayed> arrayList) {
        for(int i = 0; i<arrayList.size(); i++){
            if(arrayList.get(i).getAudioItem().getAid()==playingAudio.getAid()){
                return i;
            }
        } return -1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playingAudio.setAid(-1);
    }

    public void playAudioFile(Audio.AudioItem audioItem, String playingFrom) {
        Utils.savePlayingFrom(playingFrom,getContext());
        if(playingAudio!=null) {
            if (audioItem.getAid() == playingAudio.getAid()) {
                MainActivity.slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }else {
                AudioPlayer.intentDownload(getContext(),audioItem);
            }
        }
        else {
            AudioPlayer.intentDownload(getContext(), audioItem);
        }
    }

    public void openAudioList(String album) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment f = new AudioListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("album",album);
        f.setArguments(bundle);
        ft.replace(R.id.fragment_layout, f, "audio_list");
        ft.addToBackStack("audio_list");
        ft.commit();
    }

    public Audio.AudioItem getPlayingAudio() {
        return playingAudio;
    }

    public String getCurrentState() {
        return currentState;
    }

    @Override
    public void onResume() {
        register();
        super.onResume();
    }
}
