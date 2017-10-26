package com.mumineendownloads.mumineenaudio.Helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import br.com.goncalves.pugnotification.notification.PugNotification;
import es.dmoral.toasty.Toasty;

import static android.R.attr.id;
import static android.R.attr.progress;


public class AudioPlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,MediaPlayer.OnSeekCompleteListener
{

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PLAY_NEXT = "action_next";
    public static final String ACTION_PLAY_PREVIOUS = "action_prev";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_RESUME = "action_resume";

    public static final String FILTER_AUDIO_DATA = "filter_audio_data";

    public static final String STATE_PLAYING = "state_playing";
    public static final String STATE_PAUSED = "state_pause";
    public static final String STATE_BUFFERING = "state_buffering";

    public static final String ACTION_DURATION = "action_duration";
    public static final String ACTION_AUDIO = "action_audio";
    public static final String STATE_NULL = "state_null";
    public static final String ACTION_SEEK = "action_seek";
    public static final String ACTION_SEEK_BUFFER = "action_seek_buffer";
    public static final String ACTION_PROGRESS = "action_progress";
    private static final String ACTION_SAVE = "action_save";
    private static final String ACTION_CANCEL_DOWNLOAD = "action_cancel_download";
    private static final String ACTION_PLAY_PAUSE = "play_pause";

    private static final int NOTI_PRIMARY1 = 1100;
    private static final int NOTI_PRIMARY2 = 1101;
    private static final int NOTI_SECONDARY1 = 1200;
    private static final int NOTI_SECONDARY2 = 1201;
    private static final String ANDROID_CHANNEL_ID = "Downloading";
    private static final CharSequence ANDROID_CHANNEL_NAME = "Downloading_name";


    MediaPlayer mPlayer;
    private int resumePosition;
    private AudioManager audioManager;
    private LocalBroadcastManager mLocalBroadcast;
    private Audio.AudioItem playingAudio;
    private int buffer = 0;
    private boolean buffering = false;
    private Notification.Builder mBuilder;
    private long mLastTime;
    private long startTime;
    AudioDB db;
    private ThinDownloadManager downloadManager;
    private ArrayList<DownloadRequest> downloadRequests = new ArrayList<>();
    private boolean downloading = false;
    private DownloadRequest currentRequest;
    private Audio.AudioItem currentDownloadingAudio;
    private NotificationCompat.Builder mAudioPlayerNoti;
    private MediaSessionCompat mSession;
    private MediaController mController;
    private String state;
    private String CHANNEL_ID;
    private NotificationCompat.Builder mBuilder1;
    private NotificationManager mManager;
    Notification.Builder nb = null;
    private int downloadProgress = 0;
    private Runnable r;
    private Handler progressCounter;


    public static void intentDownload(Context context, Audio.AudioItem audioItem) {
        Intent intent = new Intent(context, AudioPlayer.class);
        intent.putExtra("audio",audioItem);
        intent.setAction(ACTION_PLAY);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Audio.AudioItem audioItem = (Audio.AudioItem) intent.getSerializableExtra("audio");
            switch (action) {
                case ACTION_PLAY:
                    playMedia(audioItem);
                    break;
                case ACTION_PAUSE:
                    pauseMedia();
                    break;
                case ACTION_STOP:
                    stopMedia();
                    break;
                case ACTION_RESUME:
                    resumeMedia();
                    break;
                case ACTION_PROGRESS:
                    int progress = intent.getIntExtra("progress",0);
                    mPlayer.seekTo(progress);
                    break;
                case ACTION_SAVE:
                    Audio.AudioItem audio = (Audio.AudioItem) intent.getSerializableExtra("audioSave");
                    saveOffline(audio);
                    break;
                case ACTION_CANCEL_DOWNLOAD:
                    downloadManager.cancel(currentRequest.getDownloadId());
                    break;
                case ACTION_PLAY_PAUSE:
                    if(mPlayer.isPlaying()){
                        pauseMedia();
                    }else {
                        resumeMedia();
                    }
            }
        }
        return START_STICKY;
    }

    private void saveOffline(final Audio.AudioItem audio) {
        String url = "http://pdf.mumineendownloads.com/download.php?id="+audio.getAid();
        Uri downloadUri = Uri.parse(url);
        Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/MumineenAudio/"+audio.getAid()+".mp3");
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        downloading = false;
                        downloadRequests.remove(0);
                        startDownloading();
                        startTime = 0;
                        PugNotification.with(getApplicationContext()).cancel(1);
                        getManager().cancelAll();
                        downloadProgress = 0;
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        PugNotification.with(getApplicationContext()).cancel(1);
                        getManager().cancelAll();
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, final long totalBytes, final long downloadedBytes, final int progress) {
                        refreshNotification(progress);
                        downloadProgress = progress;
                    }
                });


        addToDownloadRequest(downloadRequest);
        startDownloading();
    }

    private void addToDownloadRequest(DownloadRequest downloadRequest) {
        downloadRequests.add(downloadRequest);
    }

    private void startDownloading() {
        if(!downloading) {
            if (downloadRequests.size() > 0) {
                downloadManager.add(downloadRequests.get(0));
                startTime = System.currentTimeMillis();
                currentRequest = downloadRequests.get(0);
                currentDownloadingAudio = getAudio();
                downloading = true;
                showNotification();
                startProgress();
            } else {
                stopProgress();
                downloading = false;
            }
        }
    }

    private void stopProgress() {
        if(progressCounter!=null) {
            progressCounter.removeCallbacks(r);
        }
    }

    private void startProgress() {
        r = new Runnable() {
            @Override
            public void run() {
                    try {
                        progressCounter = new Handler();
                        progressCounter.postDelayed(this, 1800);
                        if(downloadProgress>0) {
                            mBuilder.setProgress(100, downloadProgress, false);
                            getManager().notify(101, mBuilder.build());
                        }
                    }catch (IllegalStateException ignored){

                    }
            }
        };

        r.run();
    }

    private boolean getFile(String s) {
        File f = new File(Environment.getExternalStorageDirectory()+"/MumineenAudio/"+s).getAbsoluteFile();
        Toasty.normal(getApplicationContext(),""+f.exists()).show();
        return f.exists();
    }

    public void refreshNotification(int progress){
        PugNotification.with(getApplicationContext())
                .load()
                .identifier(1)
                .smallIcon(android.R.drawable.stat_sys_download)
                .progress()
                .value(progress,100, false)
                .build();
     }

    private boolean offlined(int aid) {
        File f = new File(Environment.getExternalStorageDirectory()+"/MumineenAudio/"+aid+".mp3");
        return f.exists();
    }

    private void playPrevious()  {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initMusicPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnInfoListener(this);
    }

    private void playMedia(Audio.AudioItem audioItem) {
        playingAudio = audioItem;
        if(mPlayer==null){
            initMusicPlayer();
            prepareMedia();
        }else if(mPlayer.isPlaying()){
            mPlayer.stop();
            mPlayer.reset();
            prepareMedia();
        } else {
            mPlayer.stop();
            mPlayer.reset();
            prepareMedia();
        }



        new Runnable() {
            @Override
            public void run() {
                if(mPlayer != null){
                    try {
                        int mCurrentPosition = mPlayer.getCurrentPosition();
                        sendSeekBroadCast(mCurrentPosition, 0);
                        new Handler().postDelayed(this, 1000);
                    }catch (IllegalStateException ignored){

                    }
                }
            }
        }.run();
    }

    private void prepareMedia() {
        sendNewAudioBroadcast();
        String source = "";
        if(!offlined(playingAudio.getAid())) {
            source = "http://pdf.mumineendownloads.com/audio.php?id=" + playingAudio.getAid();
            sendBroadCast(ACTION_AUDIO, STATE_BUFFERING);
            try {
                mPlayer.setDataSource(source);
                mPlayer.prepareAsync();
            } catch (IOException ignored) {

            }
        }else  {
            source = Environment.getExternalStorageDirectory()+"/MumineenAudio/"+playingAudio.getAid()+".mp3";
            try {
                mPlayer.setDataSource(source);
                mPlayer.prepare();
            } catch (IOException ignored) {

            }
        }

    }

    private void stopMedia() {
        if (mPlayer == null) return;
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            resumePosition = mPlayer.getCurrentPosition();
            sendBroadCast(STATE_PAUSED,ACTION_PAUSE);
            showUpdateAudioNotification();
        }
    }

    private void resumeMedia() {
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            sendBroadCast(STATE_PLAYING,ACTION_RESUME);
            showUpdateAudioNotification();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
        buffer = progress;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        boolean autoPlay = Utils.getAutoPlay(getApplicationContext());
        if(!autoPlay) {
            if(!Utils.getRepeat(getApplicationContext())) {
                stopMedia();
                stopSelf();
                sendBroadCast(STATE_NULL,ACTION_STOP);
            } else {
                mPlayer=null;
                playMedia(playingAudio);
            }
        }else {
            if(!Utils.getRepeat(getApplicationContext())) {
                mPlayer = null;
                playingAudio = Utils.getNextForAutoPlay(getApplicationContext(), playingAudio);
                playMedia(playingAudio);
            } else {
                mPlayer.seekTo(0);
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                mPlayer.pause();
                sendBroadCast(STATE_BUFFERING,ACTION_PAUSE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mPlayer.start();
                sendBroadCast(STATE_PLAYING,ACTION_PLAY);
                break;

        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mPlayer.start();
        sendAudioBroadCast();
        showUpdateAudioNotification();
        Utils.addToRecentList(playingAudio,getApplicationContext());
    }

    private void sendNewAudioBroadcast() {
        Intent intent = new Intent(FILTER_AUDIO_DATA);
        intent.putExtra("state",STATE_BUFFERING);
        intent.putExtra("action",ACTION_AUDIO);
        intent.putExtra("audio",playingAudio);
        state = ACTION_SEEK_BUFFER;
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }

    private void sendAudioBroadCast() {
        Intent intent = new Intent(FILTER_AUDIO_DATA);
        intent.putExtra("state",STATE_PLAYING);
        intent.putExtra("action",ACTION_PLAY);
        intent.putExtra("duration",mPlayer.getDuration());
        state = ACTION_PLAY;
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    private void sendSeekBroadCast(int currentPosition, int i) {
        if(i==0) {
            Intent intent = new Intent(FILTER_AUDIO_DATA);
            intent.putExtra("action", ACTION_SEEK);
            intent.putExtra("position", currentPosition);
            intent.putExtra("status", state);
            intent.putExtra("buffer",buffer);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        db = new AudioDB(getApplicationContext());
        downloadManager = new ThinDownloadManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
        super.onCreate();
    }

    public void createAudioNotification() {
    }

    private int playPauseDrawable() {
        if(mPlayer.isPlaying()){
            return R.drawable.pause;
        }else {
            return R.drawable.play;
        }
    }

    private PendingIntent retreivePlaybackAction(int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(this, AudioPlayer.class);
        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(ACTION_PLAY_PAUSE);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 1, action, 0);
                return pendingIntent;
            case 2:
                // Skip tracks
                action = new Intent(ACTION_PLAY_NEXT);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 2, action, 0);
                return pendingIntent;
            case 3:
                // Previous tracks
                action = new Intent(ACTION_PLAY_PREVIOUS);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 3, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        androidChannel.enableLights(true);
        androidChannel.enableVibration(true);
        androidChannel.setLightColor(Color.GREEN);
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(androidChannel);

    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    private void sendBroadCast(String state,String action) {
        Intent intent = new Intent(FILTER_AUDIO_DATA);
        intent.putExtra("state",state);
        intent.putExtra("action",action);
        this.state = state;

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void showUpdateAudioNotification() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            stopMedia();
            mPlayer.release();
        }
    }

    public static void actionIntent(Context applicationContext, String action) {
        Intent intent = new Intent(applicationContext,AudioPlayer.class);
        intent.setAction(action);
        applicationContext.startService(intent);
    }

    public static void seekChanged(Context applicationContext, int progress) {
        Intent intent = new Intent(applicationContext, AudioPlayer.class);
        intent.putExtra("progress",progress);
        intent.setAction(ACTION_PROGRESS);
        applicationContext.startService(intent);
    }

    public static void intentSave(Context context, Audio.AudioItem audioItem) {
        Intent intent = new Intent(context, AudioPlayer.class);
        intent.putExtra("audioSave",audioItem);
        intent.setAction(ACTION_SAVE);
        context.startService(intent);
    }

    private String calculateEllapsedTime(long startTime, long allBytes, long downloadedBytes){
        if(downloadedBytes!=0) {
            Long elapsedTime = System.currentTimeMillis() - startTime;
            Long allTimeForDownloading = (elapsedTime * allBytes / downloadedBytes);
            Long remainingTime = allTimeForDownloading - elapsedTime;
            int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime)));
            int minute = (int) (TimeUnit.MILLISECONDS.toMinutes(remainingTime) -
                    TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(remainingTime)));
            if (minute <= 0) {
                return String.format("%2d seconds left",
                        TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime)));
            } else {
                return String.format("%d minutes left",
                        TimeUnit.MILLISECONDS.toMinutes(remainingTime) -
                                TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(remainingTime)) + 1);
            }
        } return  "Preparing";
    }

    public void showNotification() {
        PugNotification.with(getApplicationContext())
                .load()
                .title(getAudio().getTitle())
                .identifier(1)
                .smallIcon(android.R.drawable.stat_sys_download)
                .progress()
                .value(0,100, true)
                .build();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mBuilder = new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                    .setContentTitle(getAudio().getTitle())
                    .setSmallIcon(android.R.drawable.stat_sys_download,4)
                    .setProgress(0,100,true)
                    .setOnlyAlertOnce(true)
                    .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary))
                    .setOngoing(true);
            getManager().notify(101, mBuilder.build());
        }
    }

    public Audio.AudioItem getAudio() {
        int id = Integer.parseInt(currentRequest.getDestinationURI().getLastPathSegment().substring(0, 4));
        return db.getAudio(id);
    }

}
