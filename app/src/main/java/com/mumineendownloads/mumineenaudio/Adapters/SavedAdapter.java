package com.mumineendownloads.mumineenaudio.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Fragments.HomeFrament;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.Models.RecentlyPlayed;
import com.mumineendownloads.mumineenaudio.R;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_NULL;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PAUSED;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PLAYING;


public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.AudioViewHolder> {

    private final HomeFrament homeFragment;
    ArrayList<Audio.AudioItem> albumModels;
    Context context;
    private Audio.AudioItem playingAudio;
    private String currentState = STATE_NULL;
    String type = "playlist";


    public SavedAdapter(ArrayList<Audio.AudioItem> albumModels, Context context, HomeFrament homeFrament, String type){
        this.albumModels = albumModels;
        this.context = context;
        this.homeFragment = homeFrament;
        this.type = type;
    }


    @Override
    public SavedAdapter.AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent, null);
        return new AudioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SavedAdapter.AudioViewHolder holder, int position) {
        playingAudio = homeFragment.getPlayingAudio();
        currentState = homeFragment.getCurrentState();
        final Audio.AudioItem singleItem = albumModels.get(position);
        holder.title.setText(singleItem.getTitle());


        if(singleItem.getAid()==playingAudio.getAid()){
            switch (currentState) {
                case STATE_PLAYING:
                    holder.animation.setVisibility(View.VISIBLE);
                    holder.legend.setVisibility(View.GONE);
                    break;
                case STATE_PAUSED:
                    holder.animation.setVisibility(View.GONE);
                    holder.legend.setVisibility(View.VISIBLE);
                    holder.legend.setImageResource(R.drawable.ic_volume_bars);
                    break;
                default:
                    if (Utils.downloaded(context, singleItem)) {
                        holder.legend.setImageResource(R.drawable.ic_checked);
                        holder.legend.setVisibility(View.VISIBLE);
                    } else {
                        holder.legend.setVisibility(View.GONE);
                    }
                    break;
            }
        } else {
            holder.animation.setVisibility(View.GONE);
            if(Utils.downloaded(context,singleItem)) {
                holder.legend.setImageResource(R.drawable.ic_checked);
                holder.legend.setVisibility(View.VISIBLE);
            }else {
                holder.legend.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Utils.isConnected(context)){
                    if(!Utils.downloaded(context,singleItem)){
                        Toasty.normal(context,"No Internet connection").show();
                    }else {
                        homeFragment.playAudioFile(singleItem, type);
                    }
                } else {
                    homeFragment.playAudioFile(singleItem, type);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        if(albumModels.size()<20){
            return albumModels.size();
        }else {
            return 20;
        }
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView size;
        public ProgressView progressView;
        public ImageView playPause;
        public ImageView legend;
        public RelativeLayout animation;
        AudioViewHolder (View view) {
            super(view);
            title = view.findViewById(R.id.title);
            size = view.findViewById(R.id.size);
            progressView = view.findViewById(R.id.loading);
            playPause = view.findViewById(R.id.playPauseBtn);
            legend = view.findViewById(R.id.legend);
            animation = view.findViewById(R.id.animation);
            Fonty.setFonts((ViewGroup) view);
        }
    }

}
