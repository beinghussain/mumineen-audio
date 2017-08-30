package com.mumineendownloads.mumineenaudio.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.mumineendownloads.mumineenaudio.Fragments.HomeFragment;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Models.Audio;

import java.util.ArrayList;

/**
 * Created by Hussain on 8/25/2017.
 */

public class AudioAdapter extends BaseAudioAdapter {

    private final Context context;
    private ArrayList<Audio.AudioItem> audioItems;
    private HomeFragment homeFragment;
    private AudioDB audioDB;

    public AudioAdapter(ArrayList<Audio.AudioItem> audioItems, Context context, HomeFragment homeFragment) {
        super(audioItems);
        this.audioItems = audioItems;
        this.context = context;
        this.homeFragment = homeFragment;
    }


    @Override
    public boolean onPlaceSubheaderBetweenItems(int position) {
        final Audio.AudioItem pdf = audioItems.get(position);
        final Audio.AudioItem nextPdf = audioItems.get(position + 1);
        return !pdf.getCat().equals(nextPdf.getCat());
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder1, int itemPosition) {
        Audio.AudioItem audioItem = audioItems.get(itemPosition);
        final AudioViewHolder holder = ((AudioViewHolder) holder1);
        holder.title.setText(audioItem.getTitle());
    }

    @Override
    public void onBindSubheaderViewHolder(SubHeaderHolder subheaderHolder, int nextItemPosition) {
        Audio.AudioItem item = audioItems.get(nextItemPosition);
        int count = 0;
        for(Audio.AudioItem p : audioItems){
            if(item.getCat().equals(p.getCat())){
                count++;
            }
        }
        String unit = "items";
        if(count==1){
            unit="item";
        }
        subheaderHolder.mSubHeaderText.setText(item.getCat());
        subheaderHolder.mSubHeaderText.setText(item.getCat());
        subheaderHolder.itemCount.setText(count+" " + unit + " items" );
    }
}
