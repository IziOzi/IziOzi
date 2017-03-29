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
import android.content.ClipData;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOBoard;
import it.iziozi.iziozi.core.IOConfiguration;
import it.iziozi.iziozi.core.IOGlobalConfiguration;
import it.iziozi.iziozi.core.IOLevel;
import it.iziozi.iziozi.core.IOSpeakableImageButton;
import it.iziozi.iziozi.helpers.IOHelper;


public class IOBoardFragment extends Fragment implements View.OnDragListener, View.OnLongClickListener,
    View.OnTouchListener {

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

    private ImageLoader imageLoader = ImageLoader.getInstance();

    private int xPos, yPos;

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

    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();
        IOSpeakableImageButton draggedImage = (IOSpeakableImageButton) event.getLocalState();
        TextView draggedLabel = ((TextView) ((ViewGroup) draggedImage.getParent()).getChildAt(0));
        ViewGroup parentDraggedImage = (ViewGroup) draggedImage.getParent();

        ViewGroup view = (ViewGroup) v;
        IOSpeakableImageButton targetImage = (IOSpeakableImageButton) ((ViewGroup) view.getChildAt(0)).getChildAt(1);
        TextView targetLabel = (TextView) ((ViewGroup) view.getChildAt(0)).getChildAt(0);

        Vibrator vibObj = null;
        if (getActivity() != null) {
            vibObj = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }

        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                draggedImage.setVisibility(View.INVISIBLE);
                if (vibObj != null) {
                    vibObj.vibrate(10);
                }

                break;

            case DragEvent.ACTION_DRAG_ENTERED:
                // add red border around view
                targetImage.setIsHiglighted(true);
                targetImage.invalidate();
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                targetImage.setIsHiglighted(false);
                targetImage.invalidate();
                break;

            case DragEvent.ACTION_DROP:
                // if it's not the same viewgroup, i.e. drag&drop in any different position than the current one
                targetImage.setIsHiglighted(false);
                targetImage.invalidate();

                if (parentDraggedImage != view) {
                    if (parentDraggedImage.getChildCount() > 0) {
                        parentDraggedImage.removeViewAt(0);
                    }
                    if (parentDraggedImage.getChildCount() > 0) {
                        parentDraggedImage.removeViewAt(0);
                    }
                    if (((ViewGroup) view.getChildAt(0)).getChildCount() > 0) {
                        ((ViewGroup) view.getChildAt(0)).removeViewAt(0);
                    }
                    if (((ViewGroup) view.getChildAt(0)).getChildCount() > 0) {
                        ((ViewGroup) view.getChildAt(0)).removeViewAt(0);
                    }

                    parentDraggedImage.addView(targetImage, 0);
                    parentDraggedImage.addView(targetLabel, 0);
                    //Fix for issue #268

                    if(draggedImage.getParent() != null)
                        ((ViewGroup) draggedImage.getParent()).removeView(draggedImage);
                    ((ViewGroup) view.getChildAt(0)).addView(draggedImage, 0);

                    if(draggedLabel.getParent() != null)
                        ((ViewGroup) draggedLabel.getParent()).removeView(draggedLabel);
                    ((ViewGroup) view.getChildAt(0)).addView(draggedLabel, 0);

                    int targetIndex = getBoard().getButtons().indexOf(targetImage);
                    int draggedIndex = getBoard().getButtons().indexOf(draggedImage);
                    getBoard().getButtons().set(targetIndex, draggedImage);
                    getBoard().getButtons().set(draggedIndex, targetImage);
                }

                draggedImage.setVisibility(View.VISIBLE);
                break;

            case DragEvent.ACTION_DRAG_ENDED:
                // If the user dropped the image in an illegal position ACTION_DROP won't fire;
                // so make this view visible again
                if (draggedImage.getVisibility() != View.VISIBLE) {
                    draggedImage.setVisibility(View.VISIBLE);
                }
                break;
        }

        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadow = new PictogramDragShadow(v, xPos, yPos); //View.DragShadowBuilder(v);
        v.startDrag(data, shadow, v, 0);

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            xPos = (int) event.getX();
            yPos = (int) event.getY();
        }

        // It's important to return false so the event propagates to onLongClick
        return false;
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

        if(mBoard != null) {
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

                    btnContainer.setOnDragListener(this);

                    LayoutInflater layoutInflater =  (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View pictoLayout = layoutInflater.inflate(R.layout.picto_element,null);
                    final IOSpeakableImageButton originalButton = (configButtons.size() > 0 && configButtons.size() > mButtons.size()) ? configButtons.get(mButtons.size()) : new IOSpeakableImageButton();

                    final IOSpeakableImageButton imgButton = (IOSpeakableImageButton)pictoLayout.findViewById(R.id.img_button);

                    imgButton.setAudioFile(originalButton.getAudioFile());
                    imgButton.setIntentName(originalButton.getIntentName());
                    imgButton.setIntentPackageName(originalButton.getIntentPackageName());
                    imgButton.setIsMatrioska(originalButton.getIsMatrioska());
                    imgButton.setmImageFile(originalButton.getmImageFile());
                    imgButton.setmSentence(originalButton.getmSentence());
                    imgButton.setmTitle(originalButton.getmTitle());
                    imgButton.setmUrl(originalButton.getmUrl());
                    imgButton.setVideoFile(originalButton.getVideoFile());
                    imgButton.setmLevel(originalButton.getLevel());

                    imgButton.setmContext(getActivity());
                    imgButton.setShowBorder(IOConfiguration.getShowBorders());
                    if (IOGlobalConfiguration.isEditing) {
                        imgButton.setImageDrawable(getResources().getDrawable(R.drawable.logo_org));
                    }
                    else {
                        imgButton.setImageDrawable(null);
                    }
                    imgButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imgButton.setBackgroundColor(Color.TRANSPARENT);

                    // Set the listeners
                    if (IOGlobalConfiguration.isEditing) {
                        imgButton.setOnTouchListener(this);
                        imgButton.setOnLongClickListener(this);
                    } else {
                        imgButton.setOnTouchListener(null);
                        imgButton.setOnLongClickListener(null);
                    }

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
                            if (isExternalStorageReadable() && IOHelper.checkForRequiredPermissions(getActivity())) {

                                File imagesFolder = new File(IOHelper.CONFIG_BASE_DIR + File.separator + "images");

                                if (isExternalStorageWritable()) {

                                    imagesFolder.mkdirs();

                                    //download it

                                    AsyncHttpClient client = new AsyncHttpClient();
                                    client.get(imgButton.getmUrl(), new FileAsyncHttpResponseHandler(new File(imgButton.getmImageFile())) {
                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                                            Toast.makeText(getContext(), getString(R.string.download_error) + file.toString(), Toast.LENGTH_LONG).show();
                                        }


                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, File downloadedFile) {


                                            if (new File(imgButton.getmImageFile()).exists()) {
                                                imgButton.setImageBitmap(BitmapFactory.decodeFile(imgButton.getmImageFile()));
                                            } else {
                                                Toast.makeText(getContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {

                                    Toast.makeText(getContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            imageLoader.displayImage("file://" + imgButton.getmImageFile(), imgButton);
                        }
                    }else if(imgButton.getIntentName() != null && imgButton.getIntentName().length() > 0)
                    {
                        Drawable icon = null;
                        try {
                            icon = getActivity().getPackageManager().getApplicationIcon(imgButton.getIntentPackageName());
                            imgButton.setImageDrawable(icon);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    //setup labels if enabled
                    if(IOConfiguration.isShowLabels()) {
                        TextView textLabel = (TextView)pictoLayout.findViewById(R.id.img_button_label);
                        textLabel.setText(imgButton.getmTitle());
                    }
                    /*ViewGroup parent = (ViewGroup) imgButton.getParent();

                    if (parent != null)
                        parent.removeAllViews();*/

                    btnContainer.addView(pictoLayout);

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

            /*this.mBoard.setButtons(mButtons.size() > configButtons.size() ? mButtons : configButtons);*/
            this.mBoard.setButtons(mButtons);

            return tableContainer;

        }

        return null;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        if(IOHelper.checkForRequiredPermissions(getActivity())) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        if(IOHelper.checkForRequiredPermissions(getActivity())) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /*
    private Context getContext()
    {
        if(isAdded())
            return getActivity();
        return IOApplication.CONTEXT;
    }
*/

    private static class PictogramDragShadow extends View.DragShadowBuilder {

        int xPos, yPos;

        public PictogramDragShadow(View v, int xPos, int yPos) {
            super(v);
            this.xPos = xPos;
            this.yPos = yPos;
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            final View v = getView();
            if (v != null) {
                shadowSize.set(v.getWidth(), v.getHeight());
                shadowTouchPoint.set(xPos, yPos);
            }
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            super.onDrawShadow(canvas);
        }
    }
}
