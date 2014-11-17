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

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IOBoard;
import it.iziozi.iziozi.core.IOConfiguration;
import it.iziozi.iziozi.core.IOGlobalConfiguration;
import it.iziozi.iziozi.core.IOLevel;
import it.iziozi.iziozi.core.IOSpeakableImageButton;


public class IOBoardFragment extends Fragment {

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnBoardFragmentInteractionListener {

        public void tapOnSpeakableButton(IOSpeakableImageButton button, Integer level);

        public void onLevelConfigurationChanged();

    }

    private final static String LOG_TAG = "IOBoardFragment";
    private OnBoardFragmentInteractionListener mListener;

    private IOLevel mLevel = null;
    private IOBoard mBoard = null;

    private Integer mBoardLevel = 0;
    private int mBoardIndex = 0;

    /*
    * Interface widgets
    * */
    private AlertDialog mAlertDialog;
    private List<LinearLayout> homeRows = new ArrayList<LinearLayout>();


    public static IOBoardFragment newInstance(IOBoard board, IOLevel level, Integer levelIndex, int index) {
        IOBoardFragment fragment = new IOBoardFragment();
        fragment.setBoard(board);
        fragment.setLevel(level);
        fragment.setBoardLevel(levelIndex);
        fragment.setBoardIndex(index);

        return fragment;
    }

    public IOBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return buildView(IOGlobalConfiguration.isEditing);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnBoardFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }


        Log.d(LOG_TAG, "on attach!");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public IOBoard getBoard() {
        return mBoard;
    }

    public void setBoard(IOBoard mBoard) {
        this.mBoard = mBoard;
    }

    public void setLevel(IOLevel level) {
        this.mLevel = level;
    }

    public Integer getBoardLevel() {
        return mBoardLevel;
    }

    public void setBoardLevel(Integer mBoardLevel) {
        this.mBoardLevel = mBoardLevel;
    }

    public void setBoardIndex(int mBoardIndex) {
        this.mBoardIndex = mBoardIndex;
    }

    private View buildView(boolean editMode) {

        this.homeRows.clear();
        final List<IOSpeakableImageButton> mButtons = new ArrayList<IOSpeakableImageButton>();
        List<IOSpeakableImageButton> configButtons = this.mBoard.getButtons();

        ViewGroup mainView = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.table_main_layout, null);


        LinearLayout tableContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        tableContainer.setLayoutParams(mainParams);
        tableContainer.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < this.mBoard.getRows(); i++) {

            LinearLayout rowLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.f);
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            Random color = new Random();
            rowLayout.setBackgroundColor(Color.WHITE);
            tableContainer.addView(rowLayout);
            this.homeRows.add(rowLayout);
        }

        for (int j = 0; j < this.homeRows.size(); j++) {
            LinearLayout homeRow = this.homeRows.get(j);

            for (int i = 0; i < this.mBoard.getCols(); i++) {
                LinearLayout btnContainer = new LinearLayout(getActivity());
                LinearLayout.LayoutParams btnContainerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.f);
                btnContainer.setLayoutParams(btnContainerParams);
                btnContainer.setOrientation(LinearLayout.VERTICAL);
                btnContainer.setGravity(Gravity.CENTER);

                homeRow.addView(btnContainer);


                final IOSpeakableImageButton imgButton = (configButtons.size() > 0 && configButtons.size() > mButtons.size()) ? configButtons.get(mButtons.size()) : new IOSpeakableImageButton(getActivity());
                imgButton.setmContext(getActivity());
                imgButton.setShowBorder(IOConfiguration.getShowBorders());
                if(IOGlobalConfiguration.isEditing)
                    imgButton.setImageDrawable(getResources().getDrawable(R.drawable.logo_org));
                else
                    imgButton.setImageDrawable(null);
                imgButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imgButton.setBackgroundColor(Color.TRANSPARENT);


                if (imgButton.getmImageFile() != null && imgButton.getmImageFile().length() > 0) {

                    if (!new File(imgButton.getmImageFile()).exists()) {
                        if (mAlertDialog == null || !mAlertDialog.isShowing()) {
                            mAlertDialog = new AlertDialog.Builder(getActivity())
                                    .setCancelable(true)
                                    .setTitle(getString(R.string.image_missing))
                                    .setMessage(getString(R.string.image_missing_text))
                                    .setNegativeButton(getString(R.string.continue_string), null)
                                    .create();
                            mAlertDialog.show();
                        }

                        //download image

                        if (isExternalStorageReadable()) {

                            File baseFolder = new File(Environment.getExternalStorageDirectory() + "/" + IOApplication.APPLICATION_FOLDER + "/pictograms");
                            Character pictoChar = imgButton.getmImageFile().charAt(imgButton.getmImageFile().lastIndexOf("/") + 1);
                            File pictoFolder = new File(baseFolder + "/" + pictoChar + "/");

                            if (isExternalStorageWritable()) {

                                pictoFolder.mkdirs();

                                //download it

                                AsyncHttpClient client = new AsyncHttpClient();
                                client.get(imgButton.getmUrl(), new FileAsyncHttpResponseHandler(new File(imgButton.getmImageFile())) {
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                        Toast.makeText(getActivity(), getString(R.string.download_error) + file.toString(), Toast.LENGTH_LONG).show();
                                    }


                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, File downloadedFile) {


                                        if (new File(imgButton.getmImageFile()).exists()) {
                                            imgButton.setImageBitmap(BitmapFactory.decodeFile(imgButton.getmImageFile()));
                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {

                                Toast.makeText(getActivity(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                        }

                    } else
                        imgButton.setImageBitmap(BitmapFactory.decodeFile(imgButton.getmImageFile()));
                }

                ViewGroup parent = (ViewGroup) imgButton.getParent();

                if (parent != null)
                    parent.removeAllViews();

                btnContainer.addView(imgButton);

                mButtons.add(imgButton);

                imgButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int index = mButtons.indexOf(v);
                        if (mListener != null)
                            mListener.tapOnSpeakableButton(mButtons.get(index), mBoardLevel);
                    }
                });
            }
        }

        this.mBoard.setButtons(mButtons.size() > configButtons.size() ? mButtons : configButtons);

        return tableContainer;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
