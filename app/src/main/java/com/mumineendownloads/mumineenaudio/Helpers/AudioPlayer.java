package com.mumineendownloads.mumineenaudio.Helpers;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mumineendownloads.mumineenaudio.Models.Audio;
import com.mumineendownloads.mumineenaudio.R;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

import static android.R.attr.description;


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


    MediaPlayer mPlayer;
    private int resumePosition;
    private AudioManager audioManager;
    private LocalBroadcastManager mLocalBroadcast;
    private Audio.AudioItem playingAudio;
    private int buffer = 0;
    private boolean buffering = false;
    private NotificationCompat.Builder mBuilder;
    private NotificationManagerCompat mNotificationManager;
    private long mLastTime;
    private long startTime;
    AudioDB db;
    private ThinDownloadManager downloadManager;
    private ArrayList<DownloadRequest> downloadRequests = new ArrayList<>();
    private boolean downloading = false;
    private DownloadRequest currentRequest;
    private Audio.AudioItem currentDownloadingAudio;
    private NotificationCompat.Builder mAudioPlayerNoti;
    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaController mController;


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
                      mNotificationManager.cancel(0);
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        Toasty.normal(getApplicationContext(),"Download Failed").show();
                        Log.e("progress", String.valueOf(errorMessage));
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                        mBuilder.setProgress(100,progress,false);
                        mBuilder.setContentTitle(currentDownloadingAudio.getTitle());
                        mBuilder.setContentText(calculateEllapsedTime(startTime,totalBytes,downloadedBytes));
                        updateNotification();
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
            } else {
                downloading = false;
            }
        }
    }

    private boolean getFile(String s) {
        File f = new File(Environment.getExternalStorageDirectory()+"/MumineenAudio/"+s).getAbsoluteFile();
        Toasty.normal(getApplicationContext(),""+f.exists()).show();
        return f.exists();
    }

    public void updateNotification(){
        try {
            mNotificationManager.notify(0, mBuilder.build());
        }catch (IllegalArgumentException ignored){
        }
     }

    public void clearNotification(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotificationManager.cancel(0);
            }
        }, 10);
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
               // playMedia(playingAudio);
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
        createAudioNotification();
        showUpdateAudioNotification();
    }

    private void sendNewAudioBroadcast() {
        Intent intent = new Intent(FILTER_AUDIO_DATA);
        intent.putExtra("state",STATE_BUFFERING);
        intent.putExtra("action",ACTION_AUDIO);
        intent.putExtra("audio",playingAudio);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }

    private void sendAudioBroadCast() {
        Intent intent = new Intent(FILTER_AUDIO_DATA);
        intent.putExtra("state",STATE_PLAYING);
        intent.putExtra("action",ACTION_PLAY);
        intent.putExtra("duration",mPlayer.getDuration());
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
            intent.putExtra("buffer",buffer);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mAudioPlayerNoti = new NotificationCompat.Builder(getApplicationContext());

        Intent cancelIntent = new Intent(getApplicationContext(), AudioPlayer.class);
        cancelIntent.putExtra("action",ACTION_CANCEL_DOWNLOAD);
        PendingIntent cancelPendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        cancelIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.addAction(0, "Cancel Download",cancelPendingIntent);

        // Create a new MediaSession


        db = new AudioDB(getApplicationContext());
        downloadManager = new ThinDownloadManager();
        super.onCreate();
    }

    public void createAudioNotification() {
        Bitmap anImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.album_art);


        final MediaSessionCompat mediaSession;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mediaSession = new MediaSessionCompat(this, "debug tag");
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap("art",anImage)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, playingAudio.getAlbum())
                    .putString(MediaMetadata.METADATA_KEY_TITLE, playingAudio.getTitle())
                    .build());
            mediaSession.setActive(true);
            mediaSession.setCallback(new MediaSessionCompat.Callback() {

            });


            mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mAudioPlayerNoti
                    .setShowWhen(false)
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2))
                    .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary))
                    .setSmallIcon(R.drawable.notification)
                    .setContentText(playingAudio.getAlbum())
                    .setContentInfo(playingAudio.getAlbum())
                    .setContentTitle(playingAudio.getTitle())
                    .setLargeIcon(anImage)
                    .addAction(R.drawable.ic_back, "prev", retreivePlaybackAction(3))
                    .addAction(playPauseDrawable(), "pause", retreivePlaybackAction(1))
                    .addAction(R.drawable.ic_next, "next", retreivePlaybackAction(2));
        }


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


    private void sendBroadCast(String state,String action) {
        Intent intent = new Intent(FILTER_AUDIO_DATA);
        intent.putExtra("state",state);
        intent.putExtra("action",action);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void showUpdateAudioNotification() {
        mAudioPlayerNoti.mActions.clear();
        mAudioPlayerNoti
                .addAction(R.drawable.ic_back, "prev", retreivePlaybackAction(3))
                .addAction(playPauseDrawable(), "pause", retreivePlaybackAction(1))
                .addAction(R.drawable.ic_next, "next", retreivePlaybackAction(2));
        mNotificationManager.notify(2, mAudioPlayerNoti.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            stopMedia();
            mPlayer.release();
            mNotificationManager.cancelAll();
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
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Starting Download...")
                .setShowWhen(false)
                .setContentText("Please wait..")
                .setProgress(100, 0, true)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setOngoing(true);


        updateNotification();
    }

    public Audio.AudioItem getAudio() {
        int id = Integer.parseInt(currentRequest.getDestinationURI().getLastPathSegment().substring(0, 4));
        return db.getAudio(id);
    }

}
