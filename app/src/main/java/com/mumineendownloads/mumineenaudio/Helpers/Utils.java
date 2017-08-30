package com.mumineendownloads.mumineenaudio.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Utils {
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
}
