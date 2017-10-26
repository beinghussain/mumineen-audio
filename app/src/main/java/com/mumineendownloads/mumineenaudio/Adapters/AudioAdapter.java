package com.mumineendownloads.mumineenaudio.Adapters;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Fragments.AudioListFragment;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.rey.material.widget.ProgressView;

import java.io.File;
import java.util.ArrayList;

import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_BUFFERING;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_NULL;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PAUSED;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PLAYING;


public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    private final Context context;
    private ArrayList<Audio.AudioItem> audioItems;
    private AudioListFragment homeFragment;
    private AudioDB audioDB;
    private Audio.AudioItem playingAudio;
    private String currentState = STATE_NULL;
    private String state;
    private int duration;
    private int currentPosition = 0;

    public AudioAdapter(ArrayList<Audio.AudioItem> audioItems, Context context, AudioListFragment homeFragment) {
        super();
        this.audioItems = audioItems;
        this.context = context;
        this.homeFragment = homeFragment;
    }

    @Override
    public AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item, parent, false);

        return new AudioViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AudioViewHolder holder, final int position) {
        final Audio.AudioItem audioItem = audioItems.get(position);
        playingAudio = homeFragment.getPlayingAudio();
        currentState = homeFragment.getCurrentState();

        if(offlined(audioItem.getAid())){
            holder.downloaded.setVisibility(View.VISIBLE);
        }else {
            holder.downloaded.setVisibility(View.GONE);
        }


        if(Utils.isConnected(context)) {
            holder.itemView.setAlpha(1f);
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    homeFragment.playAudioFile(audioItem,"default");
                    currentPosition = position;;
                }
            });

        }else {
            if(!offlined(audioItem.getAid())){
                holder.itemView.setAlpha(0.2f);
            }else {
                holder.itemView.setAlpha(1f);
                holder.mainView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        homeFragment.playAudioFile(audioItem, "default");
                        currentPosition = position;;
                    }
                });
            }
        }


        holder.moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                homeFragment.showContextMenu(holder.moreMenu,audioItem);
            }
        });

        holder.size.setText(Utils.formatSize(audioItem.getSize()));
        holder.title.setText(audioItem.getTitle());

        if(playingAudio.getAid()!=-1) {
            if (playingAudio.getAid() == audioItem.getAid()) {
                holder.title.setTextColor(ContextCompat.getColor(context,R.color.textColor));
                holder.nowPlaying.setVisibility(View.VISIBLE);
                switch (currentState) {
                    case STATE_BUFFERING:
                        holder.loading.setVisibility(View.VISIBLE);
                        holder.imageView.setVisibility(View.GONE);
                        holder.animationView.setVisibility(View.GONE);
                        break;
                    case STATE_PLAYING:
                        holder.loading.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.GONE);
                        holder.animationView.setVisibility(View.VISIBLE);
                        holder.imageView.setImageResource(R.drawable.ic_music_notes);
                        break;
                    case STATE_PAUSED:
                        holder.loading.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.VISIBLE);
                        holder.animationView.setVisibility(View.GONE);
                        holder.imageView.setImageResource(R.drawable.ic_volume_bars);
                        break;
                    case STATE_NULL:
                        holder.title.setTextColor(ContextCompat.getColor(context,R.color.textColor));
                        holder.loading.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.VISIBLE);
                        holder.imageView.setImageResource(R.drawable.ic_music_notes);
                        holder.animationView.setVisibility(View.GONE);
                        break;
                    default:
                        holder.loading.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.GONE);
                        holder.animationView.setVisibility(View.VISIBLE);
                        holder.imageView.setImageResource(R.drawable.ic_music_notes);
                        break;
                }
            } else {
                holder.title.setTextColor(ContextCompat.getColor(context,R.color.textColor));
                holder.loading.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageResource(R.drawable.ic_music_notes);
                holder.animationView.setVisibility(View.GONE);
                holder.nowPlaying.setVisibility(View.GONE);
            }
        } else {
            holder.title.setTextColor(ContextCompat.getColor(context,R.color.textColor));
            holder.loading.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageResource(R.drawable.ic_music_notes);
            holder.animationView.setVisibility(View.GONE);
            holder.nowPlaying.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }


    private boolean offlined(int aid) {
        File f = new File(Environment.getExternalStorageDirectory() + "/MumineenAudio/" + aid + ".mp3");
        return f.exists();
    }

    @Override
    public int getItemCount() {
        return audioItems.size();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView size;
        RelativeLayout mainView,animationView;
        ImageView imageView;
        ProgressView loading;
        ImageButton moreMenu;
        TextView nowPlaying;
        ImageView downloaded;
        AudioViewHolder (View view) {
            super(view);
            title =  view.findViewById(R.id.title);
            size =  view.findViewById(R.id.size);
            mainView =  view.findViewById(R.id.mainView);
            imageView = view.findViewById(R.id.imageView);
            animationView = view.findViewById(R.id.animation);
            loading = view.findViewById(R.id.seekbarAudio);
            moreMenu = view.findViewById(R.id.menu_options);
            nowPlaying = view.findViewById(R.id.nowPlaying);
            downloaded = view.findViewById(R.id.downloaded);
            Fonty.setFonts((ViewGroup) view);
        }
    }
}
