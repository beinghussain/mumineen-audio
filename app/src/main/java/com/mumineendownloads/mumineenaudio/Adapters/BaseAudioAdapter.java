package com.mumineendownloads.mumineenaudio.Adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Fragments.HomeFragment;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;


public abstract class BaseAudioAdapter extends SectionedRecyclerViewAdapter<BaseAudioAdapter.SubHeaderHolder, RecyclerView.ViewHolder> {

    private ArrayList<Audio.AudioItem> pdfBeanArrayList;
    HomeFragment homeFragment;

    static class SubHeaderHolder extends RecyclerView.ViewHolder {

        TextView mSubHeaderText, itemCount;
        TextView downloadLeft;
        public ImageButton download_all;
        public ImageButton delete;
        public CardView cardView;


        SubHeaderHolder(View itemView) {
            super(itemView);
            this.mSubHeaderText =  itemView.findViewById(R.id.sectionHeader);
            this.itemCount = itemView.findViewById(R.id.item_count);
            this.cardView = itemView.findViewById(R.id.card);
            Fonty.setFonts((ViewGroup) itemView);
        }

    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {
//        CircularProgressBar progressBarDownload;
//        ImageView imageView;
          public TextView title;
//        public TextView size;
//        RelativeLayout mainView,cancelView;
//        LinearLayout parentView;
//        public ProgressView loading;
//        Button button;
//        ImageButton cancel;
//        CardView cardView;
//        ImageView audio;
          AudioViewHolder (View view) {
            super(view);
//            Fonty.setFonts((ViewGroup) view);
//            cardView = (CardView) view.findViewById(R.id.card);
              title = (TextView) view.findViewById(R.id.title);
//            size = (TextView) view.findViewById(R.id.size);
//            mainView = (RelativeLayout) view.findViewById(R.id.mainView);
//            imageView = (ImageView) view.findViewById(R.id.imageView);
//            button = (Button) view.findViewById(R.id.openButton);
//            progressBarDownload = (CircularProgressBar) view.findViewById(R.id.spv);
//            loading = (ProgressView) view.findViewById(R.id.loading);
//            cancel = (ImageButton) view.findViewById(R.id.cancelButton);
//            cancelView = (RelativeLayout) view.findViewById(R.id.cancel);
//            parentView = (LinearLayout) view.findViewById(R.id.parentView);
              Fonty.setFonts((ViewGroup) view);
        }
    }

    BaseAudioAdapter(ArrayList<Audio.AudioItem> itemList) {
        super();
        this.pdfBeanArrayList = itemList;
    }

    @Override
    public int getViewType(int position) {
        position = getItemPositionForViewHolder(position);
        if(pdfBeanArrayList.get(position).getCat().equals("ZeeAd")){
            return 1;
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        if(viewType==1) {
            return null;
         //   return new AdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_view, parent, false));
        }else {
            return new AudioViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, parent, false));
        }
    }

    @Override
    public SubHeaderHolder onCreateSubheaderViewHolder(ViewGroup parent, int viewType) {
        return new SubHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_section_header, parent, false));
    }

    @Override
    public int getItemSize() {
        return pdfBeanArrayList.size();
    }

//    private class AdViewHolder extends RecyclerView.ViewHolder {
//        NativeExpressAdView adView;
//        CardView cardView;
//        public AdViewHolder(View inflate) {
//            super(inflate);
//            cardView = (CardView)inflate.findViewById(R.id.card);
//            adView = new NativeExpressAdView(inflate.getContext());
//            adView.setAdUnitId("ca-app-pub-4276158682587806/7378652958");
//            adView.setAdSize(new AdSize(AdSize.FULL_WIDTH,80));
//            cardView.addView(adView);
//            Fonty.setFonts((ViewGroup) inflate);
//            AdRequest request = new AdRequest.Builder().build();
//            adView.loadAd(request);
//        }
//    }
}
