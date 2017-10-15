package com.mumineendownloads.mumineenaudio.Models;

import java.util.Date;

/**
 * Created by hussaindehgamwala on 10/15/17.
 */

public class RecentlyPlayed {
    public int id;
    public int count;
    public Date date;
    public Audio.AudioItem audioItem;


    public RecentlyPlayed(int id, int count, Date date) {
        this.id = id;
        this.count = count;
        this.date = date;
    }

    public Audio.AudioItem getAudioItem() {
        return audioItem;
    }

    public void setAudioItem(Audio.AudioItem audioItem) {
        this.audioItem = audioItem;
    }

    public RecentlyPlayed() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
