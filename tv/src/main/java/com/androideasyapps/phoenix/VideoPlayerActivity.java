package com.androideasyapps.phoenix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.androideasyapps.phoenix.dao.MediaFile;
import com.androideasyapps.phoenix.shared.AppInstance;
import com.androideasyapps.phoenix.util.MediaUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.danmaku.ijk.media.widget.MediaController;
import tv.danmaku.ijk.media.widget.VideoView;

public class VideoPlayerActivity extends Activity {
    private static final Logger log = LoggerFactory.getLogger(VideoPlayerActivity.class);

	private VideoView mVideoView;
	private View mBufferingIndicator;
	private MediaController mMediaController;

	private String mVideoPath;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("KEYS", "KEYUP: " + keyCode);
        if (keyCode==KeyEvent.KEYCODE_DPAD_RIGHT) {
            // first time we hit right/left is just shows the controller, second time will do the skip
            if (mMediaController.isShowing()) {
                mMediaController.updatePosition(30000);
            } else {
                mMediaController.show(5000);
            }
            return true;
        } else if (keyCode==KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mMediaController.isShowing()) {
                mMediaController.updatePosition(-10000);
            } else {
                mMediaController.show(5000);
            }
            return true;
        } else if (keyCode==KeyEvent.KEYCODE_DPAD_UP) {
            mMediaController.show(5000);
            return true;
        } else if (keyCode==KeyEvent.KEYCODE_DPAD_DOWN) {
            mMediaController.hide();
            return true;
        } else if (keyCode==KeyEvent.KEYCODE_BUTTON_A) {
            mMediaController.show(5000);
            mMediaController.doPauseResume();
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		Intent intent = getIntent();
//		String intentAction = intent.getAction();
//		if (!TextUtils.isEmpty(intentAction)
//				&& intentAction.equals(Intent.ACTION_VIEW)) {
//			mVideoPath = intent.getDataString();
//		}

        final MediaFile mf = AppInstance.getInstance(this).getSelectedMediaFile();
        mVideoPath = MediaUtil.getVideoURLWithEmbeddedAuth(AppInstance.getInstance(this).getServer(), mf);

        log.info("Playing: {}", mVideoPath);

		mBufferingIndicator = findViewById(R.id.buffering_indicator);
		mMediaController = new MediaController(this);
        mMediaController.setOnShownListener(new MediaController.OnShownListener() {
            @Override
            public void onShown() {
                mMediaController.setFileName(MediaUtil.getLongTitle(mf).toString());
            }
        });

		mVideoView = (VideoView) findViewById(R.id.video_view);
		mVideoView.setMediaController(mMediaController);
		mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
		mVideoView.setVideoPath(mVideoPath);
		mVideoView.requestFocus();
		mVideoView.start();


	}
}
