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
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import org.apache.http.Header;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IOConfiguration;
import it.iziozi.iziozi.core.IOSpeakableImageButton;


public class IOBoardActivity extends Activity {


    /*
    * Layout and configuration
    * */
    private IOConfiguration mConfig;
    private Boolean mIsEditing = false, mCanSpeak = false;
    private List<LinearLayout> homeRows = new ArrayList<LinearLayout>();


    /*
    * Interface Lock vars
    * */
    private Integer mUnlockTimeout = 5;
    private AlertDialog mUnlockAlert = null;
    private CountDownTimer mUnlockCountDown = null;
    private Boolean mUILocked = false;


    /*
    * Window and global objects
    * */
    private View mDecorView;
    private TextToSpeech tts;

    /*
    * Interface widgets
    * */
    private AlertDialog mAlertDialog;

    /*
    * Scan Mode vars
    * */
    private Boolean isScanMode = false;
    private int mActualScanIndex = 0;
    private Handler scanModeHandler = null;
    private Runnable scanModeRunnable = null;
    private long mScanModeDelay = 3000;

    /*
    * Neurosky Mindwave support for blink detection
    * */
    private TGDevice tgDevice;
    private BluetoothAdapter btAdapter;


    public static final int CREATE_BUTTON_CODE = 8001;

    public static final String BUTTON_IMAGE_FILE = "button_image_file";
    public static final String BUTTON_TITLE = "button_title";
    public static final String BUTTON_TEXT = "button_text";
    public static final String BUTTON_INDEX = "button_index";
    public static final String BUTTON_URL = "button_url";
    public static final String BUTTON_AUDIO_FILE = "button_audio_file";

    int newRows, newCols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDecorView = getWindow().getDecorView();


        this.mConfig = IOConfiguration.getSavedConfiguration();

        if (this.mConfig == null) {
            this.mConfig = new IOConfiguration(this);
            showHintAlert();
        } else {

            lockUI();
            this.mConfig.setContext(this);
        }

        createView();

        /*
        * Neurosky Mindwave support
        * */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            tgDevice = new TGDevice(btAdapter, handler);
        }



        this.tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (tts.isLanguageAvailable(Locale.getDefault()) >= 0)
                    tts.setLanguage(Locale.getDefault());
                else
                    tts.setLanguage(Locale.ENGLISH);

                tts.speak(getResources().getString(R.string.tts_ready), TextToSpeech.QUEUE_FLUSH, null);
                mCanSpeak = true;
            }
        });

        this.mDecorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if (visibility == View.VISIBLE && IOBoardActivity.this.mUnlockAlert == null) {
                            // TODO: The system bars are visible.
                            if(mUILocked && !mIsEditing && canGoImmersive())
                                showUnlockAlert();


                        } else {
                            // TODO: The system bars are NOT visible.
                        }
                    }
                });


    }


    private void createView() {
        setContentView(buildView());
    }

    private View buildView() {

        this.homeRows.clear();
        final List<IOSpeakableImageButton> mButtons = new ArrayList<IOSpeakableImageButton>();
        List<IOSpeakableImageButton> configButtons = this.mConfig.getButtons();

        LinearLayout mainLayout = new LinearLayout(this);
        LayoutParams mainParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mainLayout.setLayoutParams(mainParams);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < this.mConfig.getRows(); i++) {

            LinearLayout rowLayout = new LinearLayout(this);
            LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.f);
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            Random color = new Random();
            rowLayout.setBackgroundColor(Color.WHITE);
            mainLayout.addView(rowLayout);
            this.homeRows.add(rowLayout);
        }

        for (int j = 0; j < this.homeRows.size(); j++) {
            LinearLayout homeRow = this.homeRows.get(j);

            for (int i = 0; i < this.mConfig.getCols(); i++) {
                LinearLayout btnContainer = new LinearLayout(this);
                LayoutParams btnContainerParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.f);
                btnContainer.setLayoutParams(btnContainerParams);
                btnContainer.setOrientation(LinearLayout.VERTICAL);
/*
                btnContainer.setPadding(2,2,2,2);
*/
/*
                btnContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_bg));
*/
/*
                Random color = new Random();
                btnContainer.setBackgroundColor(Color.argb(255, color.nextInt(255), color.nextInt(255), color.nextInt(255)));
*/
                homeRow.addView(btnContainer);


                final IOSpeakableImageButton imgButton = (configButtons.size() > 0 && configButtons.size() > mButtons.size()) ? configButtons.get(mButtons.size()) : new IOSpeakableImageButton(this);
                imgButton.setmContext(this);
                imgButton.setShowBorder(mConfig.getShowBorders());
                imgButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                imgButton.setImageDrawable(getResources().getDrawable(R.drawable.logo_org));
                imgButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imgButton.setBackgroundColor(Color.TRANSPARENT);


                if (imgButton.getmImageFile() != null && imgButton.getmImageFile().length() > 0) {

                    if (!new File(imgButton.getmImageFile()).exists()) {
                        if (mAlertDialog == null || !mAlertDialog.isShowing()) {
                            mAlertDialog = new AlertDialog.Builder(this)
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
                                        Toast.makeText(getApplicationContext(), getString(R.string.download_error) + file.toString(), Toast.LENGTH_LONG).show();
                                    }


                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, File downloadedFile) {


                                        if (new File(imgButton.getmImageFile()).exists()) {
                                            imgButton.setImageBitmap(BitmapFactory.decodeFile(imgButton.getmImageFile()));
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {

                                Toast.makeText(getApplicationContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                        }

                    } else
                        imgButton.setImageBitmap(BitmapFactory.decodeFile(imgButton.getmImageFile()));
                }

                ViewGroup parent = (ViewGroup) imgButton.getParent();

                if (parent != null)
                    parent.removeAllViews();

                btnContainer.addView(imgButton);
                mButtons.add(imgButton);

                imgButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int index = mButtons.indexOf(v);
                        tapOnSpeakableButton(mButtons.get(index));
                    }
                });
            }
        }

        this.mConfig.setButtons(mButtons.size() > configButtons.size() ? mButtons : configButtons);
       /* while (this.mConfig.getButtons().size() < (mConfig.getCols() * mConfig.getRows()))
        {
            List<IOSpeakableImageButton> btns = mConfig.getButtons();
            btns.add(new IOSpeakableImageButton(this));
            mConfig.setButtons(btns);
        }*/

        return mainLayout;
    }

    private void tapOnSpeakableButton(IOSpeakableImageButton spkBtn) {
        if (mIsEditing) {
            //spkBtn.showInsertDialog();
            Intent cIntent = new Intent(getApplicationContext(), IOCreateButtonActivity.class);
            cIntent.putExtra(BUTTON_INDEX, mConfig.getButtons().indexOf(spkBtn));

            cIntent.putExtra(BUTTON_TEXT, spkBtn.getSentence());
            cIntent.putExtra(BUTTON_TITLE, spkBtn.getmTitle());
            cIntent.putExtra(BUTTON_IMAGE_FILE, spkBtn.getmImageFile());
            cIntent.putExtra(BUTTON_AUDIO_FILE, spkBtn.getAudioFile());

            startActivityForResult(cIntent, CREATE_BUTTON_CODE);

        } else {

            if(spkBtn.getAudioFile() != null && spkBtn.getAudioFile().length() > 0)
            {

                final MediaPlayer mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(spkBtn.getAudioFile());
                    mPlayer.prepare();

                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mPlayer.release();

                        }
                    });

                    mPlayer.start();


                } catch (IOException e) {
                    Log.e("playback_debug", "prepare() failed");
                }
            }
            else if (mCanSpeak) {
                Log.d("speakable_debug", "should say: " + spkBtn.getSentence());
                if (spkBtn.getSentence() == "")
                    tts.speak(getResources().getString(R.string.tts_nosentence), TextToSpeech.QUEUE_FLUSH, null);
                else
                    tts.speak(spkBtn.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Toast.makeText(this, getResources().getString(R.string.tts_notinitialized), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
/*
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
*/
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        Log.d("menu_debug", "inflating!");

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.editMode).setChecked(this.mIsEditing);
        menu.findItem(R.id.scanMode).setChecked(isScanMode);

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {


        if (mUILocked) {
            closeOptionsMenu();
            showUnlockAlert();
        }


        return super.onMenuOpened(featureId, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();

                View layoutView = inflater.inflate(R.layout.settings_layout, null);

                Integer rows = this.mConfig.getRows();
                Integer columns = this.mConfig.getCols();

                final CheckBox bordersCheckbox = (CheckBox) layoutView.findViewById(R.id.bordersCheckbox);

                bordersCheckbox.setChecked(mConfig.getShowBorders());

                builder.setTitle(getResources().getString(R.string.settings))
                        .setView(layoutView)
                        .setPositiveButton(getResources().getString(R.string.apply), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (newCols == 0)
                                    newCols++;
                                if (newRows == 0)
                                    newRows++;

                                IOBoardActivity.this.mConfig.setCols(newCols);
                                IOBoardActivity.this.mConfig.setRows(newRows);

                                IOBoardActivity.this.mConfig.setShowBorders(bordersCheckbox.isChecked());

                                createView();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("dialog", "dismiss and discard");
                            }
                        });

                SeekBar sRows = (SeekBar) layoutView.findViewById(R.id.seekRows);
                SeekBar sCols = (SeekBar) layoutView.findViewById(R.id.seekCols);


                final TextView rowsLbl = (TextView) layoutView.findViewById(R.id.numRowsLbl);
                final TextView colsLbl = (TextView) layoutView.findViewById(R.id.numColsLbl);

                sRows.setProgress(rows);
                sCols.setProgress(columns);

                newRows = rows;
                newCols = columns;

                rowsLbl.setText("" + rows);
                colsLbl.setText("" + columns);

                sRows.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (seekBar.getProgress() == 0)
                            seekBar.setProgress(1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        Log.d("seeking", "seek rows " + progress);
                        newRows = progress;
                        rowsLbl.setText("" + progress);
                    }
                });

                sCols.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (seekBar.getProgress() == 0)
                            seekBar.setProgress(1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        Log.d("seeking", "seek cols " + progress);
                        newCols = progress;
                        colsLbl.setText("" + progress);
                    }
                });

                builder.create().show();
                break;
            }
            case R.id.editMode: {
                Log.d("options menu", "edit mode selected");
                item.setChecked(!item.isChecked());
                mIsEditing = item.isChecked();

                if (!IOBoardActivity.this.mIsEditing)
                    IOBoardActivity.this.mConfig.save();

                break;
            }

            case R.id.scanMode: {
                Log.d("options menu", "scan mode selected");
                item.setChecked(!item.isChecked());
                isScanMode = item.isChecked();

                if (isScanMode)
                    startScanMode();
                else
                    stopScanMode();

                break;
            }

            case R.id.action_save: {
                IOBoardActivity.this.mConfig.save();
                break;
            }

            case R.id.action_about: {
                Intent aboutIntent = new Intent(getApplicationContext(), IOAboutActivity.class);
                startActivity(aboutIntent);
                break;
            }

            case R.id.action_exit: {
                finish();
                break;
            }

            case R.id.action_lock: {

                if (IOBoardActivity.this.mIsEditing)
                    IOBoardActivity.this.mConfig.save();

                IOBoardActivity.this.mIsEditing = false;

                lockUI();
                break;
            }

            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }


    /*
    * Hide both the navigation bar and the status bar.
    * SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
    * a general rule, you should design your app to hide the status bar whenever you
    * hide the navigation bar.
    * */
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    /*
    * This snippet shows the system bars. It does this by removing all the flags
    * except for the ones that make the content appear under the system bars.
    * */
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_BUTTON_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                int index = extras.getInt(BUTTON_INDEX);
                String title = extras.getString(BUTTON_TITLE);
                String text = extras.getString(BUTTON_TEXT);
                String imageFile = extras.getString(BUTTON_IMAGE_FILE);
                String imageUrl = extras.getString(BUTTON_URL);
                String audioFile = extras.getString(BUTTON_AUDIO_FILE);

                IOSpeakableImageButton button = mConfig.getButtons().get(index);

                if (text != null)
                    button.setSentence(text);

                if (title != null)
                    button.setmTitle(title);

                if (imageFile != null) {
                    button.setImageBitmap(BitmapFactory.decodeFile(imageFile));
                    button.setmImageFile(imageFile);
                }

                if (imageUrl != null)
                    button.setmUrl(imageUrl);

                button.setAudioFile(audioFile);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
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

    private void lockUI() {
        if (canGoImmersive())
            hideSystemUI();

        mUILocked = true;
    }


    private void showUnlockAlert() {
        this.mUnlockAlert = new AlertDialog.Builder(IOBoardActivity.this)
                .setTitle(getResources().getString(R.string.unlock))
                .setMessage(getResources().getQuantityString(R.plurals.unlock_question, IOBoardActivity.this.mUnlockTimeout.intValue(), IOBoardActivity.this.mUnlockTimeout.intValue()))
                .setPositiveButton(getResources().getString(R.string.unlock), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUILocked = false;
                        if (canGoImmersive() == false)
                            openOptionsMenu();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lockUI();
                    }
                })
                .setCancelable(false)
                .create();

        this.mUnlockAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                IOBoardActivity.this.mUnlockCountDown.cancel();
                IOBoardActivity.this.mUnlockCountDown = null;
                IOBoardActivity.this.mUnlockAlert = null;
            }
        });

        this.mUnlockAlert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                IOBoardActivity.this.mUnlockCountDown = new CountDownTimer(1000 * IOBoardActivity.this.mUnlockTimeout, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        int sVal = ((int) Math.ceil(millisUntilFinished / 1000.f));

                        IOBoardActivity.this.mUnlockAlert.setMessage(getResources().getQuantityString(R.plurals.unlock_question, sVal, sVal));
                    }

                    @Override
                    public void onFinish() {
                        IOBoardActivity.this.mUnlockAlert.dismiss();
                        lockUI();
                    }
                };

                IOBoardActivity.this.mUnlockCountDown.start();
            }
        });

        this.mUnlockAlert.show();
    }

    private Boolean canGoImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return true;
        return false;
    }

    private void showHintAlert() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.welcome))
                .setMessage(getString(R.string.welcome_text))
                .setPositiveButton(getResources().getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUILocked = false;
                        mIsEditing = true;
                        openOptionsMenu();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void startScanMode()
    {
        if(scanModeHandler != null)
            return;

        lockUI();
        mIsEditing = false;

        /*
        * Neurosky Mindwave support
        * */
        tgDevice.connect(true);


        scanModeHandler = new Handler();

        mActualScanIndex = 0;
        highlightButtonAtIndex(mActualScanIndex);
        scanModeRunnable = new Runnable() {
            @Override
            public void run() {

                highlightButtonAtIndex(mActualScanIndex + 1);

                scanModeHandler.postDelayed(this, mScanModeDelay);

            }
        };

        scanModeHandler.postDelayed(scanModeRunnable, mScanModeDelay);
    }

    private void highlightButtonAtIndex(int index)
    {

        int scanModeMaxIndex = mConfig.getCols() * mConfig.getRows();
        index = mod(index,scanModeMaxIndex);
        int scanModePrevIndex = mod(index - 1, scanModeMaxIndex);

        IOSpeakableImageButton button = mConfig.getButtons().get(index);
        ViewParent parentView = button.getParent();
        ViewGroup pView = (ViewGroup) parentView;
        pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.scanmode_border_bg));

        IOSpeakableImageButton prevbutton = mConfig.getButtons().get(scanModePrevIndex);
        parentView = prevbutton.getParent();
        pView = (ViewGroup) parentView;
        pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_bg));

        mActualScanIndex = index;

    }

    private int mod(int x, int y)
    {
        int result = x % y;
        return result < 0? result + y : result;
    }

    private void stopScanMode()
    {
        isScanMode = false;
        scanModeHandler.removeCallbacks(scanModeRunnable);
        scanModeHandler = null;
        scanModeRunnable = null;
        tgDevice.close();
        createView();
    }



/*
* Neurosky Mindwave support
* */


    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;


                        case TGDevice.STATE_CONNECTING:
                            break;
                        case TGDevice.STATE_CONNECTED:
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                        case TGDevice.STATE_NOT_PAIRED:
                        default:
                            break;
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
/*
                    Log.v("HelloEEG", "PoorSignal: " + msg.arg1);
*/
                    break;
                case TGDevice.MSG_ATTENTION:
/*
                    Log.v("HelloEEG", "Attention: " + msg.arg1);
*/
                    break;
                case TGDevice.MSG_RAW_DATA:
                    int rawValue = msg.arg1;
                    break;
                case TGDevice.MSG_EEG_POWER:
                    TGEegPower ep = (TGEegPower) msg.obj;
/*
                    Log.v("HelloEEG", "Delta: " + ep.delta);
*/
                    break;
                case TGDevice.MSG_BLINK:
                    Toast.makeText(getApplicationContext(),"blink!",Toast.LENGTH_SHORT).show();
                    Log.v("HelloEEG", "blink!: " + msg.arg1);
                    IOSpeakableImageButton actualButton = mConfig.getButtons().get(mActualScanIndex);
                    actualButton.callOnClick();


                default:
                    break;
            }
        }
    };
}
