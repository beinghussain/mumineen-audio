package com.mumineendownloads.mumineenaudio.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Fragments.HomeFrament;
import com.mumineendownloads.mumineenaudio.Models.AlbumModel;
import com.mumineendownloads.mumineenaudio.R;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final HomeFrament homeFragment;
    ArrayList<AlbumModel> albumModels;
    Context context;


    public AlbumAdapter(ArrayList<AlbumModel> albumModels, Context context, HomeFrament homeFrament){
        this.albumModels = albumModels;
        this.context = context;
        this.homeFragment = homeFrament;
    }


    @Override
    public AlbumAdapter.AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_card, null);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.AlbumViewHolder holder, int position) {
        final AlbumModel singleItem = albumModels.get(position);
        holder.title.setText(singleItem.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeFragment.openAudioList(singleItem.getTitle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumModels.size();
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        //public TextView size;
        AlbumViewHolder (View view) {
            super(view);
            title =  view.findViewById(R.id.title);
           // size =  view.findViewById(R.id.size);
            Fonty.setFonts((ViewGroup) view);
        }
    }

}
