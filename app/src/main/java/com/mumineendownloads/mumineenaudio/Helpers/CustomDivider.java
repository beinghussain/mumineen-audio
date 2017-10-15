package com.mumineendownloads.mumineenaudio.Helpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;


import com.mumineendownloads.mumineenaudio.Models.Audio;

import java.util.ArrayList;

/**
 * Created by Hussain on 7/6/2017.
 */

public class CustomDivider extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};


    private Drawable divider;
    private ArrayList<Audio.AudioItem> arrayList;

    public CustomDivider(Context context, ArrayList<Audio.AudioItem> arrayList) {
        try {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            this.arrayList = arrayList;
            styledAttributes.recycle();
        }catch (NullPointerException ignored){

        }
    }

    public CustomDivider(Context context, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
    }

    public CustomDivider(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        styledAttributes.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if(parent.getChildAdapterPosition(view) == state.getItemCount()-1){
            outRect.right = 0;
        }else {
            outRect.right = 15;
        }
        if(parent.getChildAdapterPosition(view) == 0){
            outRect.right = 15;
        }
    }
}