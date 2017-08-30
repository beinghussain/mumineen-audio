package com.mumineendownloads.mumineenaudio.Adapters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mumineendownloads.mumineenaudio.Activities.MainActivity;
import com.mumineendownloads.mumineenaudio.Fragments.HomeFragment;

import java.util.ArrayList;

/**
 * Created by Hussain on 8/23/2017.
 */

public class PagerAdapterTab extends FragmentPagerAdapter {
    private final ArrayList<String> arrayTabList;

    public PagerAdapterTab(FragmentManager fm, MainActivity activity, ArrayList<String> arrayTabList) {
        super(fm);
        this.arrayTabList = arrayTabList;
    }

    @Override
    public HomeFragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        HomeFragment pdfListFragment = new HomeFragment();
        pdfListFragment.setArguments(bundle);
        return pdfListFragment;
    }

    @Override
    public int getCount() {
        return arrayTabList.size();
    }

    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        for(int i = 0; i<arrayTabList.size(); i++){
            if(!arrayTabList.get(i).equals("0"))
                if(position==i){
                    title = arrayTabList.get(i);
                }
        }
        return title;
    }
}
