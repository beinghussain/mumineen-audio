package com.mumineendownloads.mumineenaudio.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.Models.RecentlyPlayed;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class Utils {
    private static Audio.AudioItem nextAudioPlayer;

    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager connec =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connec != null) {
                if (connec.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.CONNECTED || connec.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.CONNECTING || connec.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.CONNECTING || connec.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.CONNECTED) {
                    return true;

                } else if (
                        connec.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                                connec.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.DISCONNECTED) {
                    return false;
                }
            }
        }catch (NullPointerException ignored){

        }
        return false;
    }

    public static List<String> getFiles(){
        try {
            List<String> files = new ArrayList<>();
            String path = Environment.getExternalStorageDirectory().toString() + "/Mumineen Audio/";
            File directory = new File(path);

            for (File file : directory.listFiles()) {
                files.add(file.getName().replace(".pdf", ""));
            }
            return files;
        }catch (NullPointerException ignored){
            return new ArrayList<>();
        }
    }

    public static String formatSize(int s) {
        int sizeT = (int) ((long) s / 1024);
        String t;
        if ((long) s < 1000000) {
            t = (long) s / 1024 + " kb  ";
        } else {
            Float size = (float) sizeT / 1024;
            t = new DecimalFormat("##.##").format(size) + " mb  ";
        }
        return t;

    }

    public static void autoPlayTo(Context context, boolean b) {
        SharedPreferences preferences  = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("autoplay",b).apply();
    }

    public static boolean getAutoPlay(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("autoplay",false);
    }

    public static Audio.AudioItem getNextForAutoPlay(Context context, Audio.AudioItem currentAudio){
        AudioDB audioDB = new AudioDB(context);
        ArrayList<Audio.AudioItem> audioItems = audioDB.getAllPDFS(currentAudio.getAlbum());
        int index = getIndex(currentAudio,audioItems);
        return audioItems.get(index+1);
    }



    public static int getIndex(Audio.AudioItem audioItem, ArrayList<Audio.AudioItem> audioItems){
        for(int i=0; i<audioItems.size(); i++){
            if(audioItems.get(i).getAid()==audioItem.getAid()){
                return i;
            }
        } return 0;
    }

    public static String timeFormat(int seconds) {
        int hours,minutes;
        hours = seconds / 3600;
        minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        if(hours<=0){
            return String.format("%02d:%02d", minutes, seconds);
        }else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    public static Audio.AudioItem getNextAudioPlayer(Context context, Audio.AudioItem currentAudio) {
        AudioDB audioDB = new AudioDB(context);
        ArrayList<Audio.AudioItem> audioItems = audioDB.getAllPDFS(currentAudio.getAlbum());
        int index = getIndex(currentAudio,audioItems);
        return audioItems.get(index+1);
    }

    public static Audio.AudioItem getPrevAudioPlayer(Context context, Audio.AudioItem currentAudio) {
        AudioDB audioDB = new AudioDB(context);
        ArrayList<Audio.AudioItem> audioItems = audioDB.getAllPDFS(currentAudio.getAlbum());
        int index = getIndex(currentAudio,audioItems);
        return audioItems.get(index-1);
    }

    public static boolean getRepeat(Context applicationContext) {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("repeat",false);
    }

    public static void setRepeat(Context context){
        boolean repeat = getRepeat(context);
        if(repeat){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("repeat",false).apply();
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("repeat",true).apply();
        }
    }

    public static void addToPlaylist(Context context, Audio.AudioItem audioItem) {
        Log.e("Audio",audioItem.getTitle());
        ArrayList<Audio.AudioItem> audioItems = getPlaylist(context);
        if(audioExists(audioItem,audioItems)) {
            audioItems.remove(getIndexOf(audioItem,audioItems));
        }else {
            audioItems.add(audioItem);
        }
        String json = new Gson().toJson(audioItems);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("playlist",json).apply();
    }

    private static int getIndexOf(Audio.AudioItem audioItem, ArrayList<Audio.AudioItem> audioItems) {
        for(int i =0; i<audioItems.size();i++){
            if(audioItems.get(i).getAid()==audioItem.getAid()){
                return i;
            }
        }

        return -1;
    }

    private static boolean audioExists(Audio.AudioItem audioItem, ArrayList<Audio.AudioItem> audioItems) {
        for (Audio.AudioItem item : audioItems) {
            if(item.getAid()==audioItem.getAid()) {
                return true;
            }
        }

        return false;
    }

    public static ArrayList<Audio.AudioItem> getPlaylist(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString("playlist","[]");
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<Audio.AudioItem>>() {
        }.getType());
    }

    public static boolean itemExistinPlaylist(Context applicationContext, Audio.AudioItem playingAudio) {
        return audioExists(playingAudio,getPlaylist(applicationContext));
    }

    public static void addToRecentlyPlayer(Context context, Audio.AudioItem audioItem) {
        ArrayList<Audio.AudioItem> audioItems = getPlaylist(context);
        if(audioExists(audioItem,audioItems)) {
            audioItems.remove(getIndexOf(audioItem,audioItems));
        }else {
            audioItems.add(audioItem);
        }
        String json = new Gson().toJson(audioItems);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("playlist",json).apply();
    }

    public static ArrayList<Audio.AudioItem> getQueue(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString("queue","[]");
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<Audio.AudioItem>>() {
        }.getType());
    }

    public static void addToQueueList(Audio.AudioItem audioItem, Context context) {
        ArrayList<Audio.AudioItem> queuelist = getQueue(context);
        if(!audioExists(audioItem,queuelist)) {
            queuelist.add(audioItem);
        }
        String json = new Gson().toJson(queuelist);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("queue",json).apply();
    }


    public static ArrayList<RecentlyPlayed> getRecentList(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString("recent","[]");
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<RecentlyPlayed>>() {
        }.getType());
    }

    public static void addToRecentList(Audio.AudioItem audioItem, Context context) {
        ArrayList<RecentlyPlayed> recentList = getRecentList(context);
        if(!recentExists(audioItem,recentList)) {
            RecentlyPlayed played = new RecentlyPlayed();
            played.setAudioItem(audioItem);
            played.setCount(0);
            played.setId(audioItem.getAid());
            played.setDate(new Date());
            recentList.add(played);
        }else {
            try {
                RecentlyPlayed recentlyPlayed = getRecent(audioItem.getAid(), context);
                recentlyPlayed.setDate(new Date());
                recentlyPlayed.setCount(recentlyPlayed.getCount()+1);
            }catch (NullPointerException ignored){

            }
        }
        String json = new Gson().toJson(recentList);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("recent",json).apply();
    }

    public static RecentlyPlayed getRecent(int aid, Context context){
        ArrayList<RecentlyPlayed> arrayList = getRecentList(context);
        for(RecentlyPlayed played : arrayList){
            if(played.getAudioItem().getAid()==aid){
                return played;
            }
        }
        return new RecentlyPlayed();
    }

    private static boolean recentExists(Audio.AudioItem audioItem, ArrayList<RecentlyPlayed> recentList) {
        for(int i = 0; i<recentList.size(); i++){
            if(recentList.get(i).getAudioItem().getId()==audioItem.getAid()) {
                return true;
            }
        }

        return false;
    }

    public static void addAllToRecentList(ArrayList<Audio.AudioItem> audioItems, Context context){
        String json = new Gson().toJson(audioItems);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("recent",json).apply();
    }
}
