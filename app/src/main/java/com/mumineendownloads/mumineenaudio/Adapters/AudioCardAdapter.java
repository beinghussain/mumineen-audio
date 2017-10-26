package com.mumineendownloads.mumineenaudio.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Fragments.HomeFrament;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;

import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_BUFFERING;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_NULL;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PAUSED;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PLAYING;


public class AudioCardAdapter extends RecyclerView.Adapter<AudioCardAdapter.AudioViewHolder> {

    private final HomeFrament homeFragment;
    ArrayList<Audio.AudioItem> albumModels;
    Context context;
    private Audio.AudioItem playingAudio;
    private String currentState = STATE_NULL;
    String type = "playlist";


    public AudioCardAdapter(ArrayList<Audio.AudioItem> albumModels, Context context, HomeFrament homeFrament,String type){
        this.albumModels = albumModels;
        this.context = context;
        this.homeFragment = homeFrament;
        this.type = type;
    }


    @Override
    public AudioCardAdapter.AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(type.equals("playlist")) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_card, null);
        }else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent, null);
        }
        return new AudioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AudioCardAdapter.AudioViewHolder holder, int position) {
        playingAudio = homeFragment.getPlayingAudio();
        currentState = homeFragment.getCurrentState();
        final Audio.AudioItem singleItem = albumModels.get(position);
        holder.title.setText(singleItem.getTitle());
        if (singleItem.getAid() == playingAudio.getAid()) {
                switch (currentState) {
                    case STATE_BUFFERING:
                        holder.progressView.setVisibility(View.VISIBLE);
                        holder.playPause.setVisibility(View.GONE);
                        holder.playPause.setImageResource(R.drawable.play);
                        break;
                    case STATE_PLAYING:
                        holder.progressView.setVisibility(View.GONE);
                        holder.playPause.setVisibility(View.VISIBLE);
                        holder.playPause.setImageResource(R.drawable.pause);
                        break;
                    case STATE_PAUSED:
                        holder.progressView.setVisibility(View.GONE);
                        holder.playPause.setVisibility(View.VISIBLE);
                        holder.playPause.setImageResource(R.drawable.play);
                        break;
                    case STATE_NULL:
                        holder.progressView.setVisibility(View.GONE);
                        holder.playPause.setVisibility(View.VISIBLE);
                        holder.playPause.setImageResource(R.drawable.play);
                        break;
                    default:
                        holder.progressView.setVisibility(View.GONE);
                        holder.playPause.setVisibility(View.VISIBLE);
                        holder.playPause.setImageResource(R.drawable.play);
                        break;
                }
        } else {
            holder.playPause.setVisibility(View.VISIBLE);
            holder.progressView.setVisibility(View.GONE);
            holder.playPause.setImageResource(R.drawable.play);
        }

        holder.playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(singleItem.getAid()==playingAudio.getAid()) {
                    homeFragment.playPause();
                }else {
                    homeFragment.playAudioFile(singleItem, "playlist");
                }
            }
        });

        if(!Utils.isConnected(context)){
            if(!Utils.downloaded(context,singleItem)){
                holder.itemView.setAlpha(0.2f);
            }else {
                holder.itemView.setAlpha(1f);
            }
        } else {
            holder.itemView.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return albumModels.size();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView size;
        ProgressView progressView;
        ImageView playPause;
        AudioViewHolder (View view) {
            super(view);
            title =  view.findViewById(R.id.title);
            size =  view.findViewById(R.id.size);
            progressView = view.findViewById(R.id.loading);
            playPause = view.findViewById(R.id.playPauseBtn);
            Fonty.setFonts((ViewGroup) view);
        }
    }

}
