package com.mumineendownloads.mumineenaudio.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.Models.RecentlyPlayed;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        String playingFrom = getPlayingFrom(context);
        Toasty.normal(context,playingFrom).show();
        Audio.AudioItem audioItem = null;
        AudioDB audioDB = new AudioDB(context);
        switch (playingFrom){
            case "playlist":
                ArrayList<Audio.AudioItem> playlist = Utils.getPlaylist(context);
                int index = getIndex(currentAudio,playlist);
                audioItem = playlist.get(index+1);
                break;
            case "recent":
                ArrayList<Audio.AudioItem> recent = Utils.getCurrentRecentList(context);
                int index1 = getIndex(currentAudio,recent);
                audioItem = recent.get(index1+1);
                break;
            case "most":
                ArrayList<Audio.AudioItem> most = Utils.getCurrentMostList(context);
                int index2 = getIndex(currentAudio,most);
                audioItem = most.get(index2+1);
                break;
            case "saved":
                ArrayList<Audio.AudioItem> saved = Utils.getSavedList(context);
                int index3 = getIndex(currentAudio,saved);
                audioItem = saved.get(index3+1);
                break;
            default:
                ArrayList<Audio.AudioItem> audioItems = audioDB.getAllPDFS(currentAudio.getAlbum());
                int index4 = getIndex(currentAudio, audioItems);
                audioItem = audioItems.get(index4+1);
                break;
        }

        return audioItem;

    }

    public static Audio.AudioItem getPrevAudioPlayer(Context context, Audio.AudioItem currentAudio) {
        String playingFrom = getPlayingFrom(context);
        Audio.AudioItem audioItem = null;
        AudioDB audioDB = new AudioDB(context);
        switch (playingFrom){
            case "playlist":
                ArrayList<Audio.AudioItem> playlist = Utils.getPlaylist(context);
                int index = getIndex(currentAudio,playlist);
                audioItem = playlist.get(index-1);
                break;
            case "recent":
                ArrayList<RecentlyPlayed> recent = Utils.getRecentList(context);
                index = getIndexOfRecentAudio(recent,currentAudio);
                audioItem = recent.get(index-1).getAudioItem();
                break;
            case "most":
                ArrayList<RecentlyPlayed> most = Utils.getRecentList(context);
                Collections.sort(most, new Comparator<RecentlyPlayed>() {
                    @Override
                    public int compare(RecentlyPlayed o1, RecentlyPlayed o2) {
                        return o2.getCount() - (o1.getCount());
                    }
                });
                index = getIndexOfRecentAudio(most,currentAudio);
                audioItem = most.get(index-1).getAudioItem();
                break;
            case "saved":
                ArrayList<Audio.AudioItem> saved = Utils.getSavedList(context);
                index = getIndex(currentAudio,saved);
                audioItem = saved.get(index-1);
                break;
            default:
                ArrayList<Audio.AudioItem> audioItems = audioDB.getAllPDFS(currentAudio.getAlbum());
                index = getIndex(currentAudio, audioItems);
                audioItem = audioItems.get(index-1);
                break;
        }

        return audioItem;
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
                int index = getIndexOfRecent(recentList,recentlyPlayed);
                recentlyPlayed.setDate(new Date());
                recentlyPlayed.setCount(recentlyPlayed.getCount()+1);
                recentList.set(index, recentlyPlayed);
            }catch (NullPointerException ignored){}
        }


        String json = new Gson().toJson(recentList);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("recent",json).apply();
    }

    public static int getIndexOfRecent(ArrayList<RecentlyPlayed> recentlyPlayedArrayList, RecentlyPlayed recentlyPlayed){
        for(int i = 0; i<recentlyPlayedArrayList.size();i++){
            if(recentlyPlayed.getAudioItem().getAid() == recentlyPlayedArrayList.get(i).getAudioItem().getAid()){
                return i;
            }
        }return -1;
    }

    public static int getIndexOfRecentAudio(ArrayList<RecentlyPlayed> recentlyPlayedArrayList, Audio.AudioItem audio){
        for(int i = 0; i<recentlyPlayedArrayList.size(); i++){
            Log.e("Audio", String.valueOf(recentlyPlayedArrayList.get(i).getAudioItem().getTitle()));
            if(recentlyPlayedArrayList.get(i).getAudioItem().getAid()==audio.getAid()){
                Log.e("Matched","audio at "+ i);
            }
//            if(recentlyPlayedArrayList.get(i).getAudioItem().getAid() == audio.getAid()){
//                return i;
//            }
        }

        return -1;
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
            if(recentList.get(i).getAudioItem().getAid()==audioItem.getAid()) {
                return true;
            }
        }

        return false;
    }

    public static void addAllToRecentList(ArrayList<Audio.AudioItem> audioItems, Context context){
        String json = new Gson().toJson(audioItems);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("recent",json).apply();
    }

    public static boolean downloaded(Context context, Audio.AudioItem singleItem) {
        File file = new File(Environment.getExternalStorageDirectory()+"/MumineenAudio/"+singleItem.getAid()+".mp3");
        return file.exists();
    }

    public static int dpToPx(Context context) {
        try {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return Math.round(71 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }catch (NullPointerException ignored){

        }
        return 180;
    }

    public static ArrayList<Audio.AudioItem> getSavedList(Context context) {
        String path = Environment.getExternalStorageDirectory().toString()+"/MumineenAudio";
        File directory = new File(path);
        File[] files = directory.listFiles();
        AudioDB audioDB = new AudioDB(context);
        ArrayList<Audio.AudioItem> audioItems = new ArrayList<>();
        for (File file : files) {
            String[] list = file.toString().split("/");
            String id = list[list.length-1];
            int audioId = Integer.parseInt(id.replace(".mp3", ""));
            audioItems.add(audioDB.getAudio(audioId));

        }

        return audioItems;
    }

    public static void savePlayingFrom(String playingFrom, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("playingFrom",playingFrom).apply();
    }

    public static String getPlayingFrom(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return  preferences.getString("playingFrom","default");
    }

    public static void saveCurrentRecentList(ArrayList<RecentlyPlayed> recentlyPlayedArrayList, Context context) {
        ArrayList<Audio.AudioItem> audioItems = new ArrayList<>();
        for (int i = 0; i < recentlyPlayedArrayList.size(); i++){
            audioItems.add(recentlyPlayedArrayList.get(i).getAudioItem());
        }

        String json = new Gson().toJson(audioItems);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("currentRecentList",json).apply();

    }

    public static void saveCurrentMostList(ArrayList<RecentlyPlayed> mostlyPlayedArrayList, Context context) {
        ArrayList<Audio.AudioItem> audioItems = new ArrayList<>();
        for (int i = 0; i < mostlyPlayedArrayList.size(); i++){
            audioItems.add(mostlyPlayedArrayList.get(i).getAudioItem());
        }

        String json = new Gson().toJson(audioItems);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("currentMostList",json).apply();

    }

    public static ArrayList<Audio.AudioItem> getCurrentRecentList(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString("currentRecentList","[]");
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<Audio.AudioItem>>() {
        }.getType());
    }

    public static ArrayList<Audio.AudioItem> getCurrentMostList(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString("currentMostList","[]");
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<Audio.AudioItem>>() {
        }.getType());
    }
}
