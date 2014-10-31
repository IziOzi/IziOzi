/*
 * Copyright (c) 2014 Martino Lessio -
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

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.IconButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;

/**
 * Created by martinolessio on 29/10/14.
 */
public class IOMediaManagerFragment extends ListFragment {

    private final static String LOG_TAG = "IOMediaManagerFragment";

    /*
* Audio capture and playback
* */
    private MediaPlayer mPlayer = null;
    private File[] mAudioFilesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupAdapter();

    }

    private void setupAdapter() {
        File audioFolder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/recordings");

        mAudioFilesList = null;

        if (audioFolder.exists()) {
            mAudioFilesList = audioFolder.listFiles();
        }

        setListAdapter(new IOAudioFilesListAdapter(getActivity(), R.layout.audiolist_cell, mAudioFilesList));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d(LOG_TAG, "click on item: " + position);

        File audioFile = mAudioFilesList[position];

        IOCreateButtonActivity activity = (IOCreateButtonActivity) getActivity();
        activity.setAudioFile(audioFile.toString());

        activity.onBackPressed();
    }

    private void deleteFile(final File f) {
        if (f.exists()) {

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.warning))
                    .setMessage(String.format(getString(R.string.file_delete_warning), f.getName()))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            f.delete();
                            setupAdapter();

                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .create()
                    .show();

        }
    }


    private void startPlaying(File f) {

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(f.toString());
            mPlayer.prepare();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });

            mPlayer.start();


        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }


    private class IOAudioFilesListAdapter extends ArrayAdapter<File> {


        public IOAudioFilesListAdapter(Context context, int resource, File[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (null == convertView)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.audiolist_cell, parent, false);

            final File f = getItem(position);

            TextView nameText = (TextView) convertView.findViewById(R.id.audiolistFileNameTextView);
            TextView dateText = (TextView) convertView.findViewById(R.id.audioListCreationTextView);

            final SharedPreferences preferences = getActivity().getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);

            String labeledName = preferences.getString(f.getName(), "");
            if (labeledName.length() == 0)
                labeledName = f.getName();
            nameText.setText(labeledName);

            IconButton playButton = (IconButton) convertView.findViewById(R.id.audiolistPlayButton);
            IconButton deleteButton = (IconButton) convertView.findViewById(R.id.audiolistRecordButton);
            final IconButton editButton = (IconButton) convertView.findViewById(R.id.audiolistEditButton);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (f == null || mPlayer != null)
                        return;

                    startPlaying(f);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFile(f);
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    View contentView = getActivity().getLayoutInflater().inflate(R.layout.rename_alert_layout, null);

                    final EditText inputText = (EditText) contentView.findViewById(R.id.newNameEditText);

                    alert.setView(contentView);
                    alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            String value = inputText.getText().toString().trim();

                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putString(f.getName(), value);

                            editor.commit();

                            notifyDataSetInvalidated();
                        }
                    });

                    alert.setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });
                    alert.show();
                }
            });


            return convertView;
        }
    }

}
