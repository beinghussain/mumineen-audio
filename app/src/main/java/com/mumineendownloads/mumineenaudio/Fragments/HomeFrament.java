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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer;
import com.mumineendownloads.mumineenaudio.Helpers.CustomDivider;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.AlbumModel;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.Models.SectionDataModel;
import com.mumineendownloads.mumineenaudio.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

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
    private RecentAdapter queueItemAdapter;
    private ArrayList<Audio.AudioItem> playlist;
    private DownloadReceiver mReceiver;
    RelativeLayout.LayoutParams params;
    ScrollView scrollView;
    private AudioCardAdapter playlistItemAdapter;
    private ArrayList<Audio.AudioItem> queue;

    public HomeFrament() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Fonty.setFonts(getActivity());

        View v =  inflater.inflate(R.layout.fragment_home, container, false);
        scrollView = v.findViewById(R.id.scrollView);

        RecyclerView albumListRecyclerView = v.findViewById(R.id.albums);
        ArrayList<String> arrayList = new AudioDB(getContext()).getAlbums();
        allAlbums = new ArrayList<>();
        for(int i=0; i<arrayList.size(); i++) {
            AlbumModel albumModel = new AlbumModel(arrayList.get(i), i, i*2);
            allAlbums.add(albumModel);
        }
        AlbumAdapter albumAdapter = new AlbumAdapter(allAlbums,getActivity().getApplicationContext(),HomeFrament.this);
        albumListRecyclerView.setAdapter(albumAdapter);
        albumListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        albumListRecyclerView.addItemDecoration(new CustomDivider(getContext()));

        RecyclerView playListRecyclerView = v.findViewById(R.id.playlist);
        playlist = Utils.getPlaylist(getContext());
        playlistItemAdapter = new AudioCardAdapter(playlist,getActivity().getApplicationContext(),HomeFrament.this,"playlist");
        playListRecyclerView.setAdapter(playlistItemAdapter);
        playListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        playListRecyclerView.addItemDecoration(new CustomDivider(getContext()));
        playListRecyclerView.getItemAnimator().setChangeDuration(0);

        RecyclerView recentRecyclerView= v.findViewById(R.id.queue);
        queue = Utils.getQueue(getContext());
        queueItemAdapter = new RecentAdapter(queue,getActivity().getApplicationContext(),HomeFrament.this,"recent");
        recentRecyclerView.setAdapter(queueItemAdapter);
        recentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        recentRecyclerView.addItemDecoration(new CustomDivider(getContext()));
        recentRecyclerView.getItemAnimator().setChangeDuration(0);

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
                int position1 = getIndexOf(playingAudio);
                switch (action) {
                    case ACTION_AUDIO:
                        playingAudio = (Audio.AudioItem) intent.getSerializableExtra("audio");
                        currentState = STATE_BUFFERING;
                        updateAdapter();
                        break;
                    case ACTION_RESUME:
                        currentState = STATE_PLAYING;
                        updateAdapter(position1);
                        break;
                    case ACTION_PLAY:
                        currentState = STATE_PLAYING;
                        params.setMargins(0, 0, 0, 180);
                        scrollView.setLayoutParams(params);
                        updateAdapter(position1);
                        break;
                    case ACTION_PAUSE:
                        currentState = STATE_PAUSED;
                        updateAdapter(position1);
                        break;
                    case ACTION_STOP:
                        currentState = STATE_NULL;
                        params.setMargins(0, 0, 0, 0);
                        scrollView.setLayoutParams(params);
                        updateAdapter(position1);
                        break;
                }
            }
        }
    }

    public void updateAdapter(int position1) {
        playlistItemAdapter.notifyItemChanged(position1);
        queueItemAdapter.notifyItemChanged(position1);
    }

    public void updateAdapter() {
        playlistItemAdapter.notifyDataSetChanged();
        queueItemAdapter.notifyDataSetChanged();
    }

    private void register() {
        mReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioPlayer.FILTER_AUDIO_DATA);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    private int getIndexOf(Audio.AudioItem playingAudio) {
        for(int i = 0; i<playlist.size(); i++){
            if(playlist.get(i).getAid()==playingAudio.getAid()){
                return i;
            }
        } return -1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playingAudio.setAid(-1);
        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
    }

    public void playAudioFile(Audio.AudioItem audioItem) {
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
