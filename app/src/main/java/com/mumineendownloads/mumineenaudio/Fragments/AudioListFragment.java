package com.mumineendownloads.mumineenaudio.Fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Activities.MainActivity;
import com.mumineendownloads.mumineenaudio.Adapters.AudioAdapter;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.w3c.dom.Text;

import java.io.File;
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

public class AudioListFragment extends Fragment {
    public RecyclerView recyclerView;
    private DownloadReceiver mReceiver;
    private Audio.AudioItem playingAudio;
    private String currentState;
    AudioAdapter audioAdapter;
    private ArrayList<Audio.AudioItem> audioItems;
    private int position;
    private String album;
    FrameLayout.LayoutParams params;


    public AudioListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Audio.AudioItem tmp = new Audio.AudioItem();
        tmp.setAid(-1);
        playingAudio = tmp;
        super.onCreate(savedInstanceState);
        position = 0;
        Bundle bundle = this.getArguments();

        if(bundle != null){
            album = bundle.getString("album");
            MainActivity.toolbar.setTitle(album);
        }


        params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio_list, container, false);
        Fonty.setFonts((ViewGroup) rootView);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getItemAnimator().setChangeDuration(0);
        AudioDB db = new AudioDB(getContext());


        audioItems = db.getAllPDFS(album);
        Collections.sort(audioItems, new Comparator<Audio.AudioItem>() {
            @Override
            public int compare(Audio.AudioItem o1, Audio.AudioItem o2) {
                return o1.getCat().compareTo(o2.getCat());
            }
        });
        audioAdapter = new AudioAdapter(audioItems,getContext(),AudioListFragment.this);
        recyclerView.setAdapter(audioAdapter);

        return rootView;
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

    private void register() {
        mReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioPlayer.FILTER_AUDIO_DATA);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void unRegister() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        }
    }

    public void showContextMenu(final View view, final Audio.AudioItem audioItem) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_list);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView download = dialog.findViewById(R.id.download);
        TextView add_to_playlist = dialog.findViewById(R.id.add_to_playlist);
        TextView share = dialog.findViewById(R.id.share);

        RelativeLayout downloadC =  dialog.findViewById(R.id.download_c);
        RelativeLayout add_to_downloadC =  dialog.findViewById(R.id.add_to_playlist_c);
        RelativeLayout shareC =  dialog.findViewById(R.id.share_c);


        if(offlined(audioItem.getAid())){
            downloadC.setVisibility(View.GONE);
        }else {
            downloadC.setVisibility(View.VISIBLE);
        }

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        register();
    }

    private boolean offlined(int aid) {
        File f = new File(Environment.getExternalStorageDirectory()+"/MumineenAudio/"+aid+".mp3");
        return f.exists();
    }

    private void saveOffline(Audio.AudioItem audioItem) {
        AudioPlayer.intentSave(getContext(),audioItem);
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
                        audioAdapter.notifyDataSetChanged();
                        recyclerView.setLayoutParams(params);
                        break;
                    case ACTION_RESUME:
                        currentState = STATE_PLAYING;
                        audioAdapter.notifyItemChanged(position1);
                        break;
                    case ACTION_PLAY:
                        currentState = STATE_PLAYING;
                        audioAdapter.notifyItemChanged(position1);
                        params.setMargins(0, 0, 0, 180);
                        break;
                    case ACTION_PAUSE:
                        currentState = STATE_PAUSED;
                        audioAdapter.notifyItemChanged(position1);
                        break;
                    case ACTION_STOP:
                        currentState = STATE_NULL;
                        audioAdapter.notifyItemChanged(position1);
                        params.setMargins(0, 0, 0, 0);
                        recyclerView.setLayoutParams(params);
                        break;
                }
            }
        }
    }

    private int getIndexOf(Audio.AudioItem playingAudio) {
        for(int i = 0; i<audioItems.size(); i++){
            if(audioItems.get(i).getAid()==playingAudio.getAid()){
                return i;
            }
        } return -1;
    }

    public Audio.AudioItem getPlayingAudio() {
        return playingAudio;
    }

    public String getCurrentState() {
        return currentState;
    }

    @Override
    public void onPause() {
        super.onPause();
        //unRegister();
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
    }
}
