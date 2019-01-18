package com.qilinkeji.libsocket.util;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.qilinkeji.libsocket.global.App;

import java.io.IOException;

/**
 * @author zhangbo
 * @date 2018/5/4
 */

public class MediaPlayUtils {

    private static final String TAG = MediaPlayUtils.class.getSimpleName();

    private static MediaPlayUtils instance = new MediaPlayUtils();

    private MediaPlayer mediaPlayer;


    private MediaPlayUtils() {
    }

    public static MediaPlayUtils getInstance() {
        return instance;
    }


    public void playRing(int resId, boolean isLoop) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }
            AssetFileDescriptor afd = App.application.getResources().openRawResourceFd(resId);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.setLooping(isLoop);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
        }
    }

    public void stopRing() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
