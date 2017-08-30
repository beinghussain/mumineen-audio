package com.mumineendownloads.mumineenaudio.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.PrefManager;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;

import butterknife.BindView;

public class Startup extends AppCompatActivity {
    private PrefManager prefManager;
    @BindView(R.id.loading) ProgressView loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_startup);
        fetch();

        changeStatusBarColor();
    }

    private void fetch() {
        boolean connected = Utils.isConnected(getApplicationContext());
        if (connected) {
            final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://mumineendownloads.com/app/getAudioApp.php";
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new Gson();
                                final ArrayList<Audio.AudioItem> pdfBeanArrayList;
                                pdfBeanArrayList = gson.fromJson(response, new TypeToken<ArrayList<Audio.AudioItem>>() {
                                }.getType());
                                final AudioDB pdfHelper = new AudioDB(getApplicationContext());
                                final Thread task = new Thread() {
                                    @Override
                                    public void run() {
                                        ArrayList<Audio.AudioItem> localArray = pdfHelper.getAllPDFS("all");
                                        if (pdfBeanArrayList.size() < pdfHelper.getAllPDFS("all").size()) {
                                            for (Audio.AudioItem audioItem : localArray) {
                                                if (!isThere(pdfBeanArrayList, audioItem.getAid())) {
                                                    pdfHelper.deletePDF(audioItem);
                                                }
                                            }
                                        }
                                        for (int i = 0; i < pdfBeanArrayList.size(); i++) {
                                            Audio.AudioItem pdfBean = pdfBeanArrayList.get(i);
                                            pdfHelper.updateOrInsert(pdfBean);
                                            if (i == pdfBeanArrayList.size() - 1) {
                                                Startup.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        launchHomeScreen();
                                                    }
                                                });
                                            }
                                        }
                                    }
                                };

                                task.start();
                            }catch (Exception ignored){
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            queue.add(stringRequest);
        }
    }

    private boolean isThere(ArrayList<Audio.AudioItem> pdfList, int pid){
        for(Audio.AudioItem pdfBean : pdfList){
            if(pdfBean.getAid()==pid){
                return true;
            }
        }
        return false;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(Startup.this, MainActivity.class));
            }
        }, 5000);

    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

}
