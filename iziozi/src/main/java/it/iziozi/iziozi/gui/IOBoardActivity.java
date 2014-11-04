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
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IOBoard;
import it.iziozi.iziozi.core.IOGlobalConfiguration;
import it.iziozi.iziozi.core.IOSpeakableImageButton;


public class IOBoardActivity extends Activity implements IOBoardFragment.OnBoardFragmentInteractionListener {


    /*
    * Layout and configuration
    * */
    private IOBoard mActiveConfig;
    private Boolean mCanSpeak = false;
    private FrameLayout mFragmentContainer = null;
    private String mActualConfigName = null;

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

        setContentView(R.layout.activity_board);

        this.mDecorView = getWindow().getDecorView();

        mFragmentContainer = (FrameLayout) findViewById(R.id.boardActivityFragmentContainer);

        this.mActiveConfig = IOBoard.getSavedConfiguration();

        if (this.mActiveConfig == null) {
            this.mActiveConfig = new IOBoard(this);
            showHintAlert();
        } else {

            lockUI();
            this.mActiveConfig.setContext(this);
        }

        IOBoardFragment mainFragment = IOBoardFragment.newInstance(mActiveConfig, 0);
        getFragmentManager().beginTransaction()
                .add(mFragmentContainer.getId(), mainFragment)
                .commit();

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
                            if (mUILocked && !IOGlobalConfiguration.isEditing && canGoImmersive())
                                showUnlockAlert();


                        } else {
                            // TODO: The system bars are NOT visible.
                        }
                    }
                });


    }

    /*
    * OnBoardFragmentInteractionListener
    * */

    public void tapOnSpeakableButton(final IOSpeakableImageButton spkBtn, final Integer level) {
        if (IOGlobalConfiguration.isEditing) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();

            View layoutView = inflater.inflate(R.layout.editmode_alertview, null);

            builder.setTitle(getString(R.string.choose));

            builder.setView(layoutView);

            final AlertDialog dialog = builder.create();

            Switch matrioskaSwitch = (Switch) layoutView.findViewById(R.id.editModeAlertToggleBoard);
            Button editPictoButton = (Button) layoutView.findViewById(R.id.editModeAlertActionPicture);
            final Button editBoardButton = (Button) layoutView.findViewById(R.id.editModeAlertActionBoard);

            matrioskaSwitch.setChecked(spkBtn.getIsMatrioska());
            editBoardButton.setEnabled(spkBtn.getIsMatrioska());

            matrioskaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    spkBtn.setIsMatrioska(isChecked);
                    editBoardButton.setEnabled(isChecked);
                }
            });

            editPictoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //spkBtn.showInsertDialog();
                    Intent cIntent = new Intent(getApplicationContext(), IOCreateButtonActivity.class);
                    cIntent.putExtra(BUTTON_INDEX, mActiveConfig.getButtons().indexOf(spkBtn));

                    cIntent.putExtra(BUTTON_TEXT, spkBtn.getSentence());
                    cIntent.putExtra(BUTTON_TITLE, spkBtn.getmTitle());
                    cIntent.putExtra(BUTTON_IMAGE_FILE, spkBtn.getmImageFile());
                    cIntent.putExtra(BUTTON_AUDIO_FILE, spkBtn.getAudioFile());

                    startActivityForResult(cIntent, CREATE_BUTTON_CODE);

                    dialog.dismiss();
                }
            });

            editBoardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    IOBoard nestedBoard = spkBtn.getInnerBoard();

                    if (null == nestedBoard) {
                        nestedBoard = new IOBoard(IOBoardActivity.this);
                        spkBtn.setInnerBoard(nestedBoard);
                    }

                    pushBoard(nestedBoard, level + 1);

                    dialog.dismiss();
                }
            });

            dialog.show();


        } else {

            if (spkBtn.getAudioFile() != null && spkBtn.getAudioFile().length() > 0) {

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
            } else if (mCanSpeak) {
                Log.d("speakable_debug", "should say: " + spkBtn.getSentence());
                if (spkBtn.getSentence() == "")
                    tts.speak(getResources().getString(R.string.tts_nosentence), TextToSpeech.QUEUE_FLUSH, null);
                else
                    tts.speak(spkBtn.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Toast.makeText(this, getResources().getString(R.string.tts_notinitialized), Toast.LENGTH_LONG).show();
            }

            if (spkBtn.getIsMatrioska() && null != spkBtn.getInnerBoard()) {
                pushBoard(spkBtn.getInnerBoard(), level + 1);
            }
        }
    }

    @Override
    public void onRegisterBoardConfig(IOBoard config) {
        mActiveConfig = config;
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

        menu.findItem(R.id.editMode).setChecked(IOGlobalConfiguration.isEditing);
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

                Integer rows = this.mActiveConfig.getRows();
                Integer columns = this.mActiveConfig.getCols();

                final CheckBox bordersCheckbox = (CheckBox) layoutView.findViewById(R.id.bordersCheckbox);

                bordersCheckbox.setChecked(mActiveConfig.getShowBorders());

                builder.setTitle(getResources().getString(R.string.settings))
                        .setView(layoutView)
                        .setPositiveButton(getResources().getString(R.string.apply), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (newCols == 0)
                                    newCols++;
                                if (newRows == 0)
                                    newRows++;

                                IOBoardActivity.this.mActiveConfig.setCols(newCols);
                                IOBoardActivity.this.mActiveConfig.setRows(newRows);

                                IOBoardActivity.this.mActiveConfig.setShowBorders(bordersCheckbox.isChecked());

                                //TODO:createView();
                                refreshView();
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
                IOGlobalConfiguration.isEditing = item.isChecked();

                if (!IOGlobalConfiguration.isEditing)
                {
                    if(null == mActualConfigName)
                        IOBoardActivity.this.mActiveConfig.save();
                    else
                        IOBoardActivity.this.mActiveConfig.saveAs(mActualConfigName);

                }


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
                if (null == mActualConfigName)
                    IOBoardActivity.this.mActiveConfig.save();
                else
                    IOBoardActivity.this.mActiveConfig.saveAs(mActualConfigName);
                break;
            }

            case R.id.action_save_as: {

                final AlertDialog.Builder alert = new AlertDialog.Builder(this);

                View contentView = getLayoutInflater().inflate(R.layout.rename_alert_layout, null);

                final EditText inputText = (EditText) contentView.findViewById(R.id.newNameEditText);

                alert.setView(contentView);
                alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = inputText.getText().toString().trim();

                        if (value.indexOf(".xml") != -1)
                            value = value.replace(".xml", "");

                        File dirFile = new File(Environment.getExternalStorageDirectory()
                                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
                        File file = new File(dirFile.toString(), value + ".xml");

                        if (file.exists()) {
                            dialog.cancel();

                            new AlertDialog.Builder(IOBoardActivity.this)
                                    .setTitle(getString(R.string.warning))
                                    .setMessage(getString(R.string.file_already_exists))
                                    .setPositiveButton(getString(R.string.continue_string), null)
                                    .create()
                                    .show();

                        } else {
                            IOBoardActivity.this.mActiveConfig.saveAs(value);
                            mActualConfigName = value;
                        }
                    }
                });

                alert.setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        });
                alert.show();

                break;
            }

            case R.id.action_load: {

                File dirFile = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
                if (!dirFile.exists())
                    dirFile.mkdirs();

                final String[] configFiles = dirFile.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {

                        if (filename.indexOf(".xml") != -1)
                            return true;

                        return false;
                    }
                });

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);

                adapter.addAll(configFiles);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.choose))
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("media_debug", "click on item " + which);

                                String fileName = configFiles[which];
                                Log.d("board_debug", fileName);

                                FragmentManager fm = getFragmentManager();
                                while (fm.getBackStackEntryCount() > 0)
                                    fm.popBackStackImmediate();


                                mActiveConfig = IOBoard.getSavedConfiguration(fileName);

                                IOBoardFragment mainFragment = IOBoardFragment.newInstance(mActiveConfig, 0);
                                getFragmentManager().beginTransaction()
                                        .replace(mFragmentContainer.getId(), mainFragment)
                                        .commit();

                            }
                        }).setNegativeButton(getResources().getString(R.string.cancel), null)
                        .create().show();


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

                if (IOGlobalConfiguration.isEditing) {
                    if (null == mActualConfigName)
                        IOBoardActivity.this.mActiveConfig.save();
                    else
                        IOBoardActivity.this.mActiveConfig.saveAs(mActualConfigName);
                }

                IOGlobalConfiguration.isEditing = false;

                lockUI();
                break;
            }

            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void pushBoard(IOBoard board, Integer level) {
        FragmentManager fm = getFragmentManager();

        IOBoardFragment innerBoardFragment = IOBoardFragment.newInstance(board, level);

        fm.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(mFragmentContainer.getId(), innerBoardFragment)
                .addToBackStack(innerBoardFragment.toString())
                .commit();

    }

    private void refreshView() {
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(mFragmentContainer.getId());

        fm.beginTransaction()
                .detach(fragment)
                .attach(fragment)
                .commit();
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

                IOSpeakableImageButton button = mActiveConfig.getButtons().get(index);

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
                        IOGlobalConfiguration.isEditing = true;
                        //TODO:createView();
                        refreshView();
                        openOptionsMenu();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }


    private void startScanMode() {
        if (scanModeHandler != null)
            return;

        lockUI();
        IOGlobalConfiguration.isEditing = false;

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

    private void highlightButtonAtIndex(int index) {

        int scanModeMaxIndex = mActiveConfig.getCols() * mActiveConfig.getRows();
        index = mod(index, scanModeMaxIndex);
        int scanModePrevIndex = mod(index - 1, scanModeMaxIndex);

        IOSpeakableImageButton button = mActiveConfig.getButtons().get(index);
        ViewParent parentView = button.getParent();
        ViewGroup pView = (ViewGroup) parentView;
        pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.scanmode_border_bg));

        IOSpeakableImageButton prevbutton = mActiveConfig.getButtons().get(scanModePrevIndex);
        parentView = prevbutton.getParent();
        pView = (ViewGroup) parentView;
        pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_bg));

        mActualScanIndex = index;

    }

    private int mod(int x, int y) {
        int result = x % y;
        return result < 0 ? result + y : result;
    }

    private void stopScanMode() {
        isScanMode = false;
        scanModeHandler.removeCallbacks(scanModeRunnable);
        scanModeHandler = null;
        scanModeRunnable = null;
        tgDevice.close();
        //TODO:createView();
        refreshView();
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
                    Toast.makeText(getApplicationContext(), "blink!", Toast.LENGTH_SHORT).show();
                    Log.v("HelloEEG", "blink!: " + msg.arg1);
                    IOSpeakableImageButton actualButton = mActiveConfig.getButtons().get(mActualScanIndex);
                    actualButton.callOnClick();


                default:
                    break;
            }
        }
    };
}
