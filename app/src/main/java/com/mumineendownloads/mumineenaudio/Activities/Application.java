package com.mumineendownloads.mumineenaudio.Activities;

import android.app.DownloadManager;

import com.marcinorlowski.fonty.Fonty;

/**
 * Created by Hussain on 8/26/2017.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //initDownloader();
        Fonty
                .context(this)
                .regularTypeface("myfonts.ttf")
                .italicTypeface("myfonts.ttf")
                .boldTypeface("myfontsbold.ttf")
                .done();
    }

//    private void initDownloader() {
//        DownloadConfiguration configuration = new DownloadConfiguration();
//        configuration.setMaxThreadNum(1);
//        configuration.setThreadNum(1);
//        DownloadManager.getInstance().init(getApplicationContext(), configuration);
//    }
}
