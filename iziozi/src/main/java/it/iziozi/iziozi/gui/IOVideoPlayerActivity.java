/*
 * Copyright (c) 2015 Martino Lessio -
 * www.martinolessio.com
 * martino [at] iziozi [dot] org
 *
 *
 * This file is part of the IziOzi project.
 *
 * IziOzi is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * IziOzi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with IziOzi.
 * If not, see http://www.gnu.org/licenses/.
 */

package it.iziozi.iziozi.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.joanzapata.android.iconify.Iconify;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.helpers.IOHelper;

public class IOVideoPlayerActivity extends AppCompatActivity {

    public final static String VIDEO_URL = "video_url";
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iovideo_player);

        if( ! IOHelper.checkForRequiredPermissions(this) )
            finish();

        lockUI();

        TextView closeText = (TextView) findViewById(R.id.close_text);
        closeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 120);

        closeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mVideoView = (VideoView) findViewById(R.id.videoView);

        Iconify.setIcon(closeText, Iconify.IconValue.fa_times_circle_o);

        Bundle extras = getIntent().getExtras();

        String videoPath = null;

        if (extras != null) {
            videoPath = extras.getString(VIDEO_URL);
        }

        if (videoPath != null) {
            mVideoView.setVideoPath(videoPath);
            mVideoView.start();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        lockUI();
        mVideoView.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void lockUI() {
        if (IOHelper.canGoImmersive())
            hideSystemUI();

    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
