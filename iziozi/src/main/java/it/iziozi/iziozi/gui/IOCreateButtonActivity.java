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
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.Iconify;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.gui.components.IOApplicationPickerFragment;
import it.iziozi.iziozi.gui.components.IOMediaManagerFragment;
import it.iziozi.iziozi.helpers.IOHelper;


public class IOCreateButtonActivity extends AppCompatActivity {

    private final String TAG = "IOCreateButtonActivity";

    public final static String IMAGE_FILE = "image_file";
    public final static String VIDEO_FILE = "image_file";
    public final static String IMAGE_TITLE = "image_title";
    public final static String IMAGE_URL = "image_url";

    private final static int IMAGE_CAMERA_PICK_INTENT = 101;
    private final static int IMAGE_GALLERY_PICK_INTENT = 100;

    private SearchView mSearchView;
    private ImageButton mImageButton;
    private EditText mTitleText, mTextText;
    private TextView mTapHereTextView;
    private TextView mAppStatusText;

    private String mImageFile = null;
    private String mVideoFile = null;
    private String mImageTitle = null;
    private String mImageText = null;
    private String mImageUrl = null;
    private String mAudioFile = null;
    private String mIntentName = null;
    private String mIntentPackageName = null;

    private TextView mPlayIcon;
    private TextView mRecordIcon;
    private TextView mDeleteIcon;

    private ViewGroup mMainView;
    private View mRecordingOverlay;

    /*
    * Multimedia fragment manager
    * */
    private ViewGroup mOverlayView;
    private FrameLayout mFragmentContainer;

    private File mFileDir, mDestinationFile, mFile;
    private String cameraFile = null;

    private int mButtonIndex;

    /*
    * Audio capture and playback
    * */
    private static final String LOG_TAG = "AudioRecordTest";

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mImageTitle = extras.getString(IOBoardActivity.BUTTON_TITLE);
            mImageFile = extras.getString(IOBoardActivity.BUTTON_IMAGE_FILE);
            mImageText = extras.getString(IOBoardActivity.BUTTON_TEXT);
            mImageUrl = extras.getString(IOBoardActivity.BUTTON_URL);
            mAudioFile = extras.getString(IOBoardActivity.BUTTON_AUDIO_FILE);
            mIntentName = extras.getString(IOBoardActivity.BUTTON_INTENT_NAME);
            mIntentPackageName = extras.getString(IOBoardActivity.BUTTON_INTENT_PACKAGENAME);
        }

        mButtonIndex = getIntent().getExtras().getInt(IOBoardActivity.BUTTON_INDEX);

        setContentView(R.layout.create_button_activity_layout);

        mMainView = (ViewGroup) findViewById(R.id.mainLayout);

        mImageButton = (ImageButton) findViewById(R.id.CreateButtonImageBtn);
        mTitleText = (EditText) findViewById(R.id.CreateButtonTitleText);
        mTextText = (EditText) findViewById(R.id.CreateButtonTextText);
        mTapHereTextView = (TextView) findViewById(R.id.CreateButtonTapLabel);
        mAppStatusText = (TextView) findViewById(R.id.app_status_text);

        mOverlayView = (ViewGroup) findViewById(R.id.createButtonOverlayView);
        mFragmentContainer = (FrameLayout) findViewById(R.id.createButtonFragmentContainer);

        if (mImageTitle != null)
            mTitleText.setText(mImageTitle);

        if (mImageText != null)
            mTextText.setText(mImageText);

        if (mImageFile != null && mImageFile.length() > 0) {
            mImageButton.setImageBitmap(BitmapFactory.decodeFile(mImageFile));
            mTapHereTextView.setVisibility(View.INVISIBLE);

            mTapHereTextView.setVisibility(View.INVISIBLE);

        }

        if (mIntentName != null && mIntentName.length() > 0) {
            mAppStatusText.setText(mIntentName);
        }


        mPlayIcon = (TextView) findViewById(R.id.createButtonPlayIcon);
        mRecordIcon = (TextView) findViewById(R.id.createButtonRecordIcon);
        mDeleteIcon = (TextView) findViewById(R.id.createButtonDeleteIcon);

        TextView imageIcon = (TextView) findViewById(R.id.createButtonImageIcon);
        TextView saveIcon = (TextView) findViewById(R.id.createButtonSaveIcon);

        mPlayIcon.setText(null);
        mRecordIcon.setText(null);
        mDeleteIcon.setText(null);

        imageIcon.setText(null);
        saveIcon.setText(null);

        Iconify.setIcon(mPlayIcon, Iconify.IconValue.fa_play);
        Iconify.setIcon(mRecordIcon, Iconify.IconValue.fa_circle);
        Iconify.setIcon(mDeleteIcon, Iconify.IconValue.fa_trash_o);
        Iconify.setIcon(imageIcon, Iconify.IconValue.fa_picture_o);
        Iconify.setIcon(saveIcon, Iconify.IconValue.fa_save);

        mPlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(null);
            }
        });

        mRecordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAudio(null);
            }
        });

        mDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAudio(null);
            }
        });

        imageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTapOnImage(null);
            }
        });

        saveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave(null);
            }
        });

        updateAudioTextLabel();

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.data_connection_needed))
                    .setNegativeButton(getString(R.string.continue_string), null)
                    .create()
                    .show();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        String pictoFile = extras.getString(IMAGE_FILE);

        String pictoUrl = extras.getString(IMAGE_URL);
        String pictoTitle = extras.getString(IMAGE_TITLE);

        if (pictoFile != null) {
            mImageFile = pictoFile;

            mImageButton.setImageBitmap(BitmapFactory.decodeFile(pictoFile));
            mTapHereTextView.setVisibility(View.INVISIBLE);
        }

        if (pictoUrl != null) {
            mImageUrl = pictoUrl;
        }

        mImageTitle = pictoTitle;

        mTitleText.setText(pictoTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideKeyboard();
/*
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
*/

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_button, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));

        if (null != searchView) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);


            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus)
                        mSearchView.setIconified(true);
                    else
                        hideRecordingOverlay();
                }
            });
            mSearchView = searchView;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        hideRecordingOverlay();

        switch (item.getItemId()) {

            case R.id.manageMedias:
                manageMedias();
                break;

            default:
                hideOverlayView();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void manageMedias() {
        if (IOHelper.checkForRequiredPermissions(this)) {

            if (null != mOverlayView && mOverlayView.getVisibility() == View.INVISIBLE) {

                getFragmentManager().beginTransaction()
                        .add(mFragmentContainer.getId(), new IOMediaManagerFragment())
                        .commit();

                AlphaAnimation a = new AlphaAnimation(0.f, 1.f);
                a.setDuration(500);

                mOverlayView.setVisibility(View.VISIBLE);
                mOverlayView.startAnimation(a);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (mOverlayView != null && View.VISIBLE == mOverlayView.getVisibility()) {
            hideOverlayView();
        } else if (null != mRecordingOverlay)
            hideRecordingOverlay();
        else {
//            super.onBackPressed();
            showExitDialog(this);
        }
    }

    private void showExitDialog(final Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit))
                .setCancelable(false)
                .setMessage(getString(R.string.alert_dialog_exit))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void updateAudioTextLabel() {
        TextView audioTextView = (TextView) findViewById(R.id.audioFileStatusTextView);

        if (null != mAudioFile && mAudioFile.length() > 0) {
            final SharedPreferences preferences = getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);

            File aFile = new File(mAudioFile);

            String labeledName = preferences.getString(aFile.getName(), "");
            if (labeledName.length() == 0)
                labeledName = aFile.getName();
            audioTextView.setText(labeledName);
        } else {
            audioTextView.setText(getString(R.string.no_audio_file));
        }
    }

    private void hideOverlayView() {


        if (mOverlayView != null && mOverlayView.getVisibility() == View.VISIBLE) {

            AlphaAnimation a = new AlphaAnimation(1.0f, 0.f);
            a.setDuration(500);

            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mOverlayView.setVisibility(View.INVISIBLE);

                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(mFragmentContainer.getId()))
                            .commit();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mOverlayView.startAnimation(a);
        }
    }

    public void doSave(View v) {
        Intent resultIntent = new Intent();
        if (mImageFile != null)
            resultIntent.putExtra(IOBoardActivity.BUTTON_IMAGE_FILE, mImageFile);

        mImageTitle = mTitleText.getText().toString();

        if (mImageTitle != null && mImageTitle.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_TITLE, mImageTitle);

        mImageText = mTextText.getText().toString();

        if (mImageText != null && mImageText.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_TEXT, mImageText);

        if (mImageUrl != null && mImageUrl.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_URL, mImageUrl);

        if (mAudioFile != null && mAudioFile.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_AUDIO_FILE, mAudioFile);

        if (mVideoFile != null && mVideoFile.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_VIDEO_FILE, mVideoFile);

        if (mIntentName != null && mIntentName.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_INTENT_NAME, mIntentName);

        if (mIntentPackageName != null && mIntentPackageName.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_INTENT_PACKAGENAME, mIntentPackageName);

        resultIntent.putExtra(IOBoardActivity.BUTTON_INDEX, mButtonIndex);

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, getString(R.string.save_done), Toast.LENGTH_SHORT).show();

        finish();
    }

    public void doTapOnImage(View v) {

        hideKeyboard();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        adapter.add(getResources().getString(R.string.img_search));
        adapter.add(getResources().getString(R.string.img_gallery));
        adapter.add(getResources().getString(R.string.img_camera));

        adapter.add(getString(R.string.add_intent));
        adapter.add(getString(R.string.remove_intent));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choose))
                .setAdapter(adapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("media_debug", "click on item " + which);
                        if (which == 0)
                            searchImage();
                        else if (which == 1)
                            pickFromGallery();
                        else if (which == 2 && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
                            pickFromCamera();
                        else if (which == 3) {
                            //Link application
                            doLinkApplication();
                        } else if (which == 4) {
                            //remove application
                            doUnlinkApplication();
                        }

                    }
                }).setNegativeButton(getResources().getString(R.string.cancel), null)
                .create().show();


    }

    private void doLinkApplication() {

        if (null != mOverlayView && mOverlayView.getVisibility() == View.INVISIBLE) {

            IOApplicationPickerFragment pickerFragment = IOApplicationPickerFragment.getInstance(new IOApplicationPickerFragment.IOApplicationSelectionListener() {
                @Override
                public void onApplicationSelected(ResolveInfo resolveInfo) {

                    mIntentName = resolveInfo.activityInfo.name;
                    mIntentPackageName = resolveInfo.activityInfo.applicationInfo.packageName;

                    mAppStatusText.setText(mIntentName);

                    if (mImageFile == null || mImageFile.length() == 0) {
                        mTapHereTextView.setVisibility(View.INVISIBLE);

                        Drawable icon = null;
                        try {
                            icon = getPackageManager().getApplicationIcon(mIntentPackageName);
                            mImageButton.setImageDrawable(icon);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                    onBackPressed();

                }
            });

            getFragmentManager().beginTransaction()
                    .add(mFragmentContainer.getId(), pickerFragment)
                    .commit();

            AlphaAnimation a = new AlphaAnimation(0.f, 1.f);
            a.setDuration(500);

            mOverlayView.setVisibility(View.VISIBLE);
            mOverlayView.startAnimation(a);
        }

    }

    private void doUnlinkApplication() {
        mIntentName = "";
        mIntentPackageName = "";

        mAppStatusText.setText(getString(R.string.no_application));
    }

    private void searchImage() {
        mSearchView.setIconified(false);
        mSearchView.requestFocus();

    }

    private void pickFromCamera() {

        if (IOHelper.checkForRequiredPermissions(this)) {

            mFileDir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/camera");
            if (!mFileDir.isDirectory())
                mFileDir.mkdirs();

            mDestinationFile = new File(mFileDir, new Date().getTime() + ".jpg");
            cameraFile = mDestinationFile.getAbsolutePath();
            try {
                if (!mDestinationFile.createNewFile())
                    Log.e("check", "unable to create empty file");

                mFile = new File(mDestinationFile.getAbsolutePath());
                Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mDestinationFile));
                startActivityForResult(i, IMAGE_CAMERA_PICK_INTENT);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void pickFromGallery() {
        if (IOHelper.checkForRequiredPermissions(this)) {

            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("image/*,video/*");
            startActivityForResult(pickIntent, IMAGE_GALLERY_PICK_INTENT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_GALLERY_PICK_INTENT: {

                    boolean isVideo;

                    ContentResolver contentResolver = getContentResolver();

                    String type = contentResolver.getType(data.getData());

                    isVideo = type.contains("video");

                    Log.d(TAG, "data type is: " + type);

                    if (isVideo) {

                        Uri selectedVideo = data.getData();

                        String sourceFilename = getRealPathFromURI(this, selectedVideo);

                        mFileDir = new File(Environment.getExternalStorageDirectory()
                                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/video");
                        if (!mFileDir.isDirectory())
                            mFileDir.mkdirs();


                        String extension = sourceFilename.substring(sourceFilename.lastIndexOf("."));

                        mDestinationFile = new File(mFileDir, new Date().getTime() + extension);


                        BufferedInputStream bis = null;
                        BufferedOutputStream bos = null;

                        try {
                            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
                            bos = new BufferedOutputStream(new FileOutputStream(mDestinationFile, false));
                            byte[] buf = new byte[1024];
                            bis.read(buf);
                            do {
                                bos.write(buf);
                            } while (bis.read(buf) != -1);
                        } catch (IOException e) {

                        } finally {
                            try {
                                if (bis != null) bis.close();
                                if (bos != null) bos.close();
                            } catch (IOException e) {

                            }
                        }

                        mVideoFile = mDestinationFile.toString();


                        Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(mDestinationFile.toString(), MediaStore.Images.Thumbnails.MINI_KIND);

                        mFileDir = new File(Environment.getExternalStorageDirectory()
                                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/gallery");
                        if (!mFileDir.isDirectory())
                            mFileDir.mkdirs();

                        mDestinationFile = new File(mFileDir, new Date().getTime() + ".jpg");

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(mDestinationFile);

                            videoThumbnail.compress(Bitmap.CompressFormat.PNG, 90, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        mImageButton.setImageBitmap(videoThumbnail);
                        mImageFile = mDestinationFile.toString();
                        mTapHereTextView.setVisibility(View.INVISIBLE);


                    } else {
                        //is image

                        Uri selectedImage = data.getData();
                        InputStream imageStream;
                        try {
                            imageStream = getContentResolver().openInputStream(selectedImage);
                            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                            if(null != bitmap) {
                                Log.d("image_debug", "onActivityResult:" + bitmap.getWidth() + " " + bitmap.getHeight());
                                if (bitmap.getHeight() >= 2048 || bitmap.getWidth() >= 2048) {
                                    bitmap = scaleToFill(bitmap, 1024, 1024);
                                }

                                mFileDir = new File(Environment.getExternalStorageDirectory()
                                        .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/gallery");
                                if (!mFileDir.isDirectory())
                                    mFileDir.mkdirs();

                                mDestinationFile = new File(mFileDir, new Date().getTime() + ".jpg");

                                FileOutputStream out = null;
                                try {
                                    out = new FileOutputStream(mDestinationFile);

                                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (out != null) {
                                            out.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }


                                mImageButton.setImageBitmap(bitmap);
                                mImageFile = mDestinationFile.toString();
                            }
                            mTapHereTextView.setVisibility(View.INVISIBLE);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                }

                case IMAGE_CAMERA_PICK_INTENT: {
                    if (mFile == null) {
                        if (cameraFile != null)
                            mFile = new File(cameraFile);
                        else
                            Log.e("check", "camera file object null");
                    } else
                        Log.e("check", mFile.getAbsolutePath());
                    Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
                    if(null != bitmap) {
                        if (bitmap.getHeight() >= 2048 || bitmap.getWidth() >= 2048) {
                            bitmap = scaleToFill(bitmap, 1024, 1024);
                        }

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(cameraFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        mImageButton.setImageBitmap(bitmap);
                        mImageFile = mFile.toString();
                    }
                    break;
                }

                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Bitmap scaleToFill(Bitmap b, int width, int height) {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);
    }


    public void recordAudio(View v) {
        if (IOHelper.checkForRequiredPermissions(this)) {

            hideKeyboard();
            onRecord(true);
        }
    }

    public void clearAudio(View v) {
        Log.d("audio_debug", "audio file:" + mAudioFile);
        mAudioFile = null;
        updateAudioTextLabel();

        Toast.makeText(this, getString(R.string.audio_deleted), Toast.LENGTH_SHORT).show();
    }

    public void playAudio(View v) {

        if (IOHelper.checkForRequiredPermissions(this)) {

            if (mAudioFile == null || (mPlayer != null && mPlayer.isPlaying())) {

                if (mPlayer == null)
                    return;

                onPlay(false);
                return;
            }

            onPlay(true);
        }
    }

    public String getAudioFile() {
        return mAudioFile;
    }

    public void setAudioFile(String mAudioFile) {
        this.mAudioFile = mAudioFile;
        updateAudioTextLabel();
    }

    /*
    * Audio capture and playback methods
    * */


    private void onRecord(boolean start) {
        if (null == mRecordingOverlay) {

            mRecordingOverlay = getLayoutInflater().inflate(R.layout.recording_overlay_view, null);
            mRecordingOverlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            TextView iconTextView = (TextView) mRecordingOverlay.findViewById(R.id.recordingTextView);
            iconTextView.setText(null);
            Iconify.setIcon(iconTextView, Iconify.IconValue.fa_microphone);

            mMainView.addView(mRecordingOverlay);
            AlphaAnimation a = new AlphaAnimation(0.f, 1.f);
            a.setDuration(500);
            mRecordingOverlay.startAnimation(a);

            AnimationSet animationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
            mRecordingOverlay.findViewById(R.id.recordingTextView).startAnimation(animationSet);

            startRecording();
        } else {

            hideRecordingOverlay();

        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void hideRecordingOverlay() {

        if (null == mRecordingOverlay)
            return;


        AlphaAnimation a = new AlphaAnimation(1.0f, 0.f);
        a.setDuration(500);

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                stopRecording();
                mMainView.removeView(mRecordingOverlay);
                mRecordingOverlay = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mRecordingOverlay.startAnimation(a);
    }


    private void startPlaying() {

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mPlayer.setDataSource(mAudioFile);
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
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {

        mFileDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/recordings");
        if (!mFileDir.isDirectory())
            mFileDir.mkdirs();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setAudioSamplingRate(44100);


        mAudioFile = mFileDir + "/" + new Date().getTime() + ".mp4";
        mRecorder.setOutputFile(mAudioFile);


        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();

    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        updateAudioTextLabel();
    }


}
