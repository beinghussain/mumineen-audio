package com.mumineendownloads.mumineenaudio.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;
import com.mumineendownloads.mumineenaudio.Adapters.PagerAdapterTab;
import com.mumineendownloads.mumineenaudio.Fragments.AudioListFragment;
import com.mumineendownloads.mumineenaudio.Fragments.HomeFrament;
import com.mumineendownloads.mumineenaudio.Helpers.AudioDB;
import com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer;
import com.mumineendownloads.mumineenaudio.Helpers.Utils;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.rey.material.widget.ProgressView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_AUDIO;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_RESUME;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.ACTION_SEEK;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_NULL;
import static com.mumineendownloads.mumineenaudio.Helpers.AudioPlayer.STATE_PLAYING;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public ViewPager viewPager;
    public PagerAdapterTab viewPagerAdapter;
    //public TabLayout tabLayout;
    public static SlidingUpPanelLayout slidingPaneLayout;
    public AudioDataReceiver localBroadcastManager;
    public TextView title,album, titleMain, albumMain;
    public ImageButton next, prev;
    public ImageButton playpause;
    public String currentState;
    public RelativeLayout bottomBar, secondToolbar;
    private AudioPlayer player;
    boolean serviceBound = false;
    private static Audio.AudioItem playingAudio;
    private int duration;
    private ImageButton playPauseBtnMain;
    private ProgressView loading;
    private SeekBar seekbarMain;
    private SlidingUpPanelLayout.PanelState panelState;
    private ImageButton closePanel;
    private RelativeLayout loadingMain;
    private TextView currentDuration,currentPosition;
    public static final int RC_STORAGE = 1;
    private ImageButton nextMain;
    private ImageButton prevMain;
    private ImageButton repeat, playlist;
    private TabLayout tabLayout;
    private TextView playingOnlineOffline;
    public static Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        Fonty.setFonts(toolbar);
        Fonty.setFonts(this);
        playingAudio = new Audio.AudioItem();
        playingAudio.setAid(-1);
        //viewPager = (ViewPager) findViewById(R.id.viewpager);
        ArrayList<String> stringArrayList = new ArrayList<>();
        AudioDB pdfHelper = new AudioDB(getApplicationContext());
        stringArrayList = pdfHelper.getAlbums();
         //viewPagerAdapter = new PagerAdapterTab(getSupportFragmentManager(),MainActivity.this,stringArrayList);
        //viewPager.setAdapter(viewPagerAdapter);
       // viewPager.setOffscreenPageLimit(6);
        //tabLayout = (TabLayout) findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(viewPager);
        slidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding);
        title= (TextView) findViewById(R.id.title);
        album =(TextView) findViewById(R.id.album);
        titleMain= (TextView) findViewById(R.id.titleMain);
        albumMain =(TextView) findViewById(R.id.albumMain);
        prev = (ImageButton) findViewById(R.id.prev);
        next = (ImageButton) findViewById(R.id.next);
        prevMain = (ImageButton) findViewById(R.id.prevMain);
        nextMain = (ImageButton) findViewById(R.id.nextMain);
        playpause = (ImageButton) findViewById(R.id.playPauseBtn);
        bottomBar = (RelativeLayout)findViewById(R.id.dragView);
        secondToolbar = (RelativeLayout)findViewById(R.id.secondToolbar);
        playPauseBtnMain = (ImageButton) findViewById(R.id.playPauseBtnM);
        loading = (ProgressView) findViewById(R.id.loading);
        seekbarMain = (SeekBar) findViewById(R.id.seekBar);
        closePanel = (ImageButton) findViewById(R.id.closePanel);
        //loadingMain = (RelativeLayout) findViewById(R.id.loadingMain);
        currentDuration = (TextView) findViewById(R.id.duration);
        currentPosition = (TextView) findViewById(R.id.currentPosition);
        repeat = (ImageButton) findViewById(R.id.repeat);
        playlist = (ImageButton) findViewById(R.id.playlist);
        playingOnlineOffline = (TextView) findViewById(R.id.playingOnlineOffline);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ActivityManager.TaskDescription taskDesc = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, getResources().getColor(R.color.colorPrimaryDark));
            setTaskDescription(taskDesc);
        }

        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();

        Fragment myFrag = new HomeFrament();
        fragTransaction.add(R.id.fragment_layout, myFrag,"home_fragment");
        fragTransaction.addToBackStack("home_fragment");
        fragTransaction.commit();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                RC_STORAGE);

                    }
                }
            }
        },5000);

        title.setSelected(true);

        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentState.equals(AudioPlayer.STATE_PAUSED)){
                    AudioPlayer.actionIntent(getApplicationContext(),AudioPlayer.ACTION_RESUME);
                }
                else if(currentState.equals(STATE_PLAYING)) {
                    AudioPlayer.actionIntent(getApplicationContext(),AudioPlayer.ACTION_PAUSE);
                }
            }
        });

        playPauseBtnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentState.equals(AudioPlayer.STATE_PAUSED)){
                    AudioPlayer.actionIntent(getApplicationContext(),AudioPlayer.ACTION_RESUME);
                }
                else if(currentState.equals(STATE_PLAYING)) {
                    AudioPlayer.actionIntent(getApplicationContext(),AudioPlayer.ACTION_PAUSE);
                }
            }
        });

        nextMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Audio.AudioItem audioItem = Utils.getNextAudioPlayer(getApplicationContext(),playingAudio);
                playAudioFile(audioItem);

            }
        });

        prevMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Audio.AudioItem audioItem = Utils.getPrevAudioPlayer(getApplicationContext(),playingAudio);
                playAudioFile(audioItem);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Audio.AudioItem audioItem = Utils.getNextAudioPlayer(getApplicationContext(),playingAudio);
                playAudioFile(audioItem);

            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Audio.AudioItem audioItem = Utils.getPrevAudioPlayer(getApplicationContext(),playingAudio);
                playAudioFile(audioItem);
            }
        });

        closePanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        if(playingAudio!=null) {
            if (Utils.itemExistinPlaylist(getApplicationContext(), playingAudio)) {
                playlist.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            } else {
                playlist.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorUnactive));
            }
        }

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.addToPlaylist(getApplicationContext(),playingAudio);
                if(!Utils.itemExistinPlaylist(getApplicationContext(),playingAudio)){
                    playlist.setImageResource(R.drawable.play_list);
                    playlist.setAlpha(0.4f);
                } else {
                    playlist.setImageResource(R.drawable.play_list_added);
                    playlist.setAlpha(1f);
                }
            }
        });


        if(Utils.getRepeat(getApplicationContext())) {
            repeat.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorUnactive));
            repeat.setAlpha(1f);
        }else {
            repeat.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorUnactive));
            repeat.setAlpha(0.4f);
        }

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setRepeat(getApplicationContext());
                if(Utils.getRepeat(getApplicationContext())) {
                    repeat.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorUnactive));
                    repeat.setAlpha(1f);
                }else {
                    repeat.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorUnactive));
                    repeat.setAlpha(0.4f);
                }
            }
        });

        Fonty.setFonts(tabLayout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerElevation(0);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUpSlidingPanelLayout();

        seekbarMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSeekChanged(seekBar.getProgress());
            }
        });
    }

    private void sendSeekChanged(int progress) {
        AudioPlayer.seekChanged(getApplicationContext(),progress);
    }

    private void setUpSlidingPanelLayout() {
        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        panelState = SlidingUpPanelLayout.PanelState.HIDDEN;

        slidingPaneLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        slidingPaneLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(slideOffset>0.02){
                    bottomBar.setVisibility(View.GONE);
                    secondToolbar.setVisibility(View.VISIBLE);
                }else {
                    bottomBar.setVisibility(View.VISIBLE);
                    secondToolbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                 panelState = newState;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(slidingPaneLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        View view = item.getActionView();
        Switch aSwitch = view.findViewById(R.id.switchForActionBar);
        boolean autoPlay = Utils.getAutoPlay(getApplicationContext());
        aSwitch.setChecked(autoPlay);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Utils.autoPlayTo(getApplicationContext(),b);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static Audio.AudioItem getPlayingAudio() {
        return playingAudio;
    }

    private class AudioDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                String state = intent.getStringExtra("state");
                String action = intent.getStringExtra("action");
                switch (action) {
                    case ACTION_AUDIO:
                        playingAudio = (Audio.AudioItem) intent.getSerializableExtra("audio");
                        seekbarMain.setProgress(0);
                        seekbarMain.setSecondaryProgress(0);
                        if(!offlined(playingAudio.getAid())){
                            playingOnlineOffline.setText("Playing Online");
                        }else {
                            playingOnlineOffline.setText("Playing Offline");
                        }
                        setState(state, action, intent);
                        break;
                    case ACTION_SEEK:
                        int positon = intent.getIntExtra("position", 0);
                        seekbarMain.setProgress(positon);
                        currentPosition.setText(Utils.timeFormat(positon/1000));
                        if(duration!=0) {
                            seekbarMain.setMax(duration);
                            currentDuration.setText(Utils.timeFormat(duration/1000));
                            int buffer = intent.getIntExtra("buffer", 0);
                            double buffer1 = (buffer/100.000)*duration;
                            seekbarMain.setSecondaryProgress((int) buffer1);
                        }
                        break;
                    default:
                        setState(state, action, intent);
                        break;
                }
            }
        }
    }

    private boolean offlined(int aid) {
        File f = new File(Environment.getExternalStorageDirectory() + "/MumineenAudio/" + aid + ".mp3");
        return f.exists();
    }

    public void playPause(String play) {
        if(play.equals("play")){
            playPauseBtnMain.setImageResource(R.drawable.play_main);
            playpause.setImageResource(R.drawable.play_main);
        } else {
            playPauseBtnMain.setImageResource(R.drawable.pause_main);
            playpause.setImageResource(R.drawable.pause_main);
        }
    }

    private void setState(String state, String action, Intent intent) {
        currentState = state;
        switch (state){
            case STATE_PLAYING:
                if(action.equals(ACTION_RESUME)) {
                    playPause("pause");
                }else {
                    if(panelState == SlidingUpPanelLayout.PanelState.HIDDEN){
                        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    title.setText(playingAudio.getTitle());
                    album.setText(playingAudio.getAlbum());
                    titleMain.setText(playingAudio.getTitle());
                    albumMain.setText(playingAudio.getAlbum());
                    int duration1  = intent.getIntExtra("duration",0);
                    if(duration1 > 0){
                        duration = duration1;
                    }
                    loading.setVisibility(View.INVISIBLE);
                    playpause.setVisibility(View.VISIBLE);
                    playPauseBtnMain.setVisibility(View.VISIBLE);
                    playPause("pause");
                }
                break;
            case AudioPlayer.STATE_PAUSED:
                playPause("play");
                break;
            case AudioPlayer.STATE_BUFFERING:
                title.setText(playingAudio.getTitle());
                album.setText(playingAudio.getAlbum());
                titleMain.setText(playingAudio.getTitle());
                albumMain.setText(playingAudio.getAlbum());
                playPause("play");
                loading.setVisibility(View.VISIBLE);
                playpause.setVisibility(View.INVISIBLE);
                playPauseBtnMain.setVisibility(View.VISIBLE);
                playPauseBtnMain.setImageResource(R.drawable.play_main);
                break;
            case STATE_NULL:
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                playingAudio = null;
                title.setText("");
                album.setText("");
                titleMain.setText("");
                albumMain.setText("");
                playPause("pause");

        }

        if(playingAudio!=null) {
            if (!Utils.itemExistinPlaylist(getApplicationContext(), playingAudio)) {
                playlist.setImageResource(R.drawable.play_list);
                playlist.setAlpha(0.4f);
            } else {
                playlist.setImageResource(R.drawable.play_list_added);
                playlist.setAlpha(1f);
            }
        }
    }

    private void register() {
        localBroadcastManager = new AudioDataReceiver();
        IntentFilter intentFilter = new IntentFilter(AudioPlayer.FILTER_AUDIO_DATA);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(localBroadcastManager, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void unRegister() {
        if (localBroadcastManager != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(localBroadcastManager);
        }
    }

    public void playAudioFile(Audio.AudioItem audioItem) {
        AudioPlayer.intentDownload(getApplicationContext(),audioItem);
    }

}
