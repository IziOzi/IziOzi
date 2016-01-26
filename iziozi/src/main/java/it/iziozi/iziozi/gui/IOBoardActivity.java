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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.IconTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.Iconify;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IOBoard;
import it.iziozi.iziozi.core.IOConfiguration;
import it.iziozi.iziozi.core.IOGlobalConfiguration;
import it.iziozi.iziozi.core.IOLevel;
import it.iziozi.iziozi.core.IOSpeakableImageButton;
import it.iziozi.iziozi.helpers.IOHelper;


public class IOBoardActivity extends AppCompatActivity implements IOBoardFragment.OnBoardFragmentInteractionListener, IOPaginatedBoardFragment.OnFragmentInteractionListener {


    /*
    * Layout and configuration
    * */
    private IOConfiguration mActiveConfig;
    private IOLevel mActualLevel;
    private Boolean mCanSpeak = false;
    private String mActualConfigName = null;

    /*
    * MediaPlayer vars
    * */

    private final MediaPlayer mPlayer = new MediaPlayer();

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
    private int mActualScanIndex = 0;
    private Handler scanModeHandler = null;
    private Runnable scanModeRunnable = null;
    private long mScanModeDelay = 5000;
    private int mScanModeMaxIndex;


    /*
    * Neurosky Mindwave support for blink detection
    * */
/*
    private TGDevice tgDevice;
    private BluetoothAdapter btAdapter;
*/

    /*
    * Pagination vars
    * */
    private int mActualIndex = 0;
    private IconTextView mLeftNavigationButton;
    private IconTextView mRightNavigationButton;
    private IconTextView mLeftEditButton;
    private IconTextView mRightEditButton;
    private IconTextView mCenterTrashNavigationButton;
    private FrameLayout mFrameLayout;

    private IconTextView mCenterBackButton;
    private IconTextView mCenterHomeButton;

    private String mPlayingFile = null;

    /*
    * side navigation vars
    * */
    private IOSpeakableImageButton mLeftSideArrowButton;
    private IOSpeakableImageButton mRightSideArrowButton;

    public static final int CREATE_BUTTON_CODE = 8001;

    public static final String BUTTON_IMAGE_FILE = "button_image_file";
    public static final String BUTTON_TITLE = "button_title";
    public static final String BUTTON_TEXT = "button_text";
    public static final String BUTTON_INDEX = "button_index";
    public static final String BUTTON_URL = "button_url";
    public static final String BUTTON_AUDIO_FILE = "button_audio_file";
    public static final String BUTTON_VIDEO_FILE = "button_video_file";
    public static final String BUTTON_INTENT_NAME = "button_intent_name";
    public static final String BUTTON_INTENT_PACKAGENAME = "button_intent_packagename";

    int newRows, newCols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_board);

        this.mDecorView = getWindow().getDecorView();

        if (IOHelper.checkForRequiredPermissions(this)) {
            this.mActiveConfig = IOConfiguration.getSavedConfiguration();
        }

        SharedPreferences preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);

        this.mActualConfigName = preferences.getString(IOGlobalConfiguration.IO_LAST_BOARD_USED, null);
        if (mActualConfigName != null)
            mActualConfigName = mActualConfigName.replace(".xml", "");

        if (this.mActiveConfig == null) {
            this.mActiveConfig = new IOConfiguration();
            showHintAlert();
        } else {
            lockUI();
        }

        mActualLevel = mActiveConfig.getLevel();

        mFrameLayout = (FrameLayout) findViewById(R.id.mainLayoutTableContainer);

        FragmentManager fm = getSupportFragmentManager();

        fm.beginTransaction()
                .add(mFrameLayout.getId(), IOPaginatedBoardFragment.newInstance(mActualLevel))
                .commit();

        setupNavButtons();

        setupSideNavButtons();

        /*
        * Neurosky Mindwave support
        * */
/*
        if (IOHelper.checkForRequiredPermissions(this)) {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null) {
                tgDevice = new TGDevice(btAdapter, handler);
            }
        }
*/

        this.tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    Locale locale = Locale.getDefault();
                    if (null == locale)
                        locale = Locale.ITALIAN;

                    if (tts.isLanguageAvailable(locale) >= 0)
                        tts.setLanguage(Locale.getDefault());
                    else
                        tts.setLanguage(Locale.ENGLISH);

                    mCanSpeak = true;
                } else {
                    Toast.makeText(IOBoardActivity.this, getString(R.string.tts_unavailable), Toast.LENGTH_LONG).show();
                    mCanSpeak = false;
                }
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
                            if (mUILocked && !IOGlobalConfiguration.isEditing && IOHelper.canGoImmersive())
                                showUnlockAlert();


                        } else {
                            // TODO: The system bars are NOT visible.
                        }
                    }
                });
    }


    private void showSideNavButtons() {
        mLeftSideArrowButton.setVisibility(View.VISIBLE);
        mRightSideArrowButton.setVisibility(View.VISIBLE);
    }

    private void hideSideNavButtons() {
        mLeftSideArrowButton.setVisibility(View.GONE);
        mRightSideArrowButton.setVisibility(View.GONE);
    }


    private void setupSideNavButtons() {
        ViewGroup leftContainer = (ViewGroup) findViewById(R.id.leftnav_container);
        ViewGroup rightContainer = (ViewGroup) findViewById(R.id.rightnav_container);

        IOSpeakableImageButton leftArrow = new IOSpeakableImageButton(this);
        IOSpeakableImageButton rightArrow = new IOSpeakableImageButton(this);

        int size = (int) (getResources().getDimension(R.dimen.nav_buttons_size) * getResources().getDisplayMetrics().densityDpi / 160);

        leftArrow.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        rightArrow.setLayoutParams(new ViewGroup.LayoutParams(size, size));

        leftArrow.setImageDrawable(getResources().getDrawable(R.drawable.freccia_sx));
        rightArrow.setImageDrawable(getResources().getDrawable(R.drawable.freccia_dx));

        leftArrow.setShowBorder(mActiveConfig.getShowBorders());
        rightArrow.setShowBorder(mActiveConfig.getShowBorders());

        leftContainer.addView(leftArrow);

        rightContainer.addView(rightArrow);

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paginateLeft();
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paginateRight();
            }
        });

        mRightSideArrowButton = rightArrow;
        mLeftSideArrowButton = leftArrow;

        hideSideNavButtons();

    }

    private void checkScanModeButtons() {

    }


    private void setupNavButtons() {

        LinearLayout centerNavigationLayout = (LinearLayout) findViewById(R.id.centerLayoutNavigationContainer);
        LinearLayout leftNavigationLayout = (LinearLayout) findViewById(R.id.leftLayoutNavigationContainer);
        LinearLayout rightNavigationLayout = (LinearLayout) findViewById(R.id.rightLayoutNavigationContainer);

        mCenterBackButton = new IconTextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.navigation_bar_button_size), (int) getResources().getDimension(R.dimen.navigation_bar_button_size));
        mCenterBackButton.setLayoutParams(params);
        mCenterBackButton.setGravity(Gravity.CENTER);
        mCenterBackButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mCenterBackButton.setTextColor(Color.WHITE);

        mCenterBackButton.setTextSize(32);
        Iconify.setIcon(mCenterBackButton, Iconify.IconValue.fa_arrow_up);

        centerNavigationLayout.addView(mCenterBackButton);

        mCenterBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IOGlobalConfiguration.isInSwapMode = false;
                Log.d("test", "back stack size " + getSupportFragmentManager().getBackStackEntryCount());
                if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getSupportFragmentManager().popBackStackImmediate();
                refreshView();
            }
        });


        mCenterHomeButton = new IconTextView(this);

        params.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()), 0, 0, 0);
        mCenterHomeButton.setLayoutParams(params);
        mCenterHomeButton.setGravity(Gravity.CENTER);
        mCenterHomeButton.setTextSize(32);
        mCenterHomeButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mCenterHomeButton.setTextColor(Color.WHITE);

        Iconify.setIcon(mCenterHomeButton, Iconify.IconValue.fa_home);

        centerNavigationLayout.addView(mCenterHomeButton);

        mCenterHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                while (fm.getBackStackEntryCount() > 0)
                    fm.popBackStackImmediate();

                refreshView();

            }
        });

        mLeftNavigationButton = new IconTextView(this);

        params.setMargins(0, 0, 0, 0);
        mLeftNavigationButton.setLayoutParams(params);
        mLeftNavigationButton.setGravity(Gravity.CENTER);
        mLeftNavigationButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mLeftNavigationButton.setTextColor(Color.WHITE);

        mLeftNavigationButton.setTextSize(32);
        Iconify.setIcon(mLeftNavigationButton, Iconify.IconValue.fa_arrow_left);

        leftNavigationLayout.addView(mLeftNavigationButton);

        mLeftNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paginateLeft();
            }
        });

        mLeftNavigationButton.setVisibility(View.INVISIBLE);

        mLeftEditButton = new IconTextView(this);

        params.setMargins(20, 0, 0, 0);
        mLeftEditButton.setLayoutParams(params);
        mLeftEditButton.setGravity(Gravity.CENTER);
        mLeftEditButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mLeftEditButton.setTextColor(Color.WHITE);

        mLeftEditButton.setTextSize(32);
        Iconify.setIcon(mLeftEditButton, Iconify.IconValue.fa_plus);

        leftNavigationLayout.addView(mLeftEditButton);

        mLeftEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (IOGlobalConfiguration.isEditing) {
                    mActualLevel.addInnerBoardAtIndex(new IOBoard(), mActualIndex);
                    refreshView();
                }
            }
        });

        mRightEditButton = new IconTextView(this);

        mRightEditButton.setLayoutParams(params);
        mRightEditButton.setGravity(Gravity.CENTER);
        mRightEditButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mRightEditButton.setTextColor(Color.WHITE);

        mRightEditButton.setTextSize(32);
        Iconify.setIcon(mRightEditButton, Iconify.IconValue.fa_plus);

        rightNavigationLayout.addView(mRightEditButton);

        mRightEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (IOGlobalConfiguration.isEditing) {
                    mActualLevel.addInnerBoardAtIndex(new IOBoard(), mActualIndex + 1);

                    refreshView(mActualLevel.getActiveIndex() + 1);
                }
            }
        });

        mRightEditButton.setVisibility(View.GONE);
        mLeftEditButton.setVisibility(View.GONE);

        mRightNavigationButton = new IconTextView(this);

        mRightNavigationButton.setLayoutParams(params);
        mRightNavigationButton.setGravity(Gravity.CENTER);
        mRightNavigationButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mRightNavigationButton.setTextColor(Color.WHITE);

        mRightNavigationButton.setTextSize(32);
        Iconify.setIcon(mRightNavigationButton, Iconify.IconValue.fa_arrow_right);

        rightNavigationLayout.addView(mRightNavigationButton);

        mRightNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paginateRight();

            }
        });

        if (mActualLevel.getLevelSize() == 1)
            mRightNavigationButton.setVisibility(View.INVISIBLE);


        mCenterTrashNavigationButton = new IconTextView(this);

        mCenterTrashNavigationButton.setLayoutParams(params);
        mCenterTrashNavigationButton.setGravity(Gravity.CENTER);
        mCenterTrashNavigationButton.setBackground(getResources().getDrawable(R.drawable.circular_shape));
        mCenterTrashNavigationButton.setTextColor(Color.WHITE);

        mCenterTrashNavigationButton.setTextSize(32);
        Iconify.setIcon(mCenterTrashNavigationButton, Iconify.IconValue.fa_trash_o);

        centerNavigationLayout.addView(mCenterTrashNavigationButton);

        mCenterTrashNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(IOBoardActivity.this)
                        .setTitle(getResources().getString(R.string.warning))
                        .setMessage(getString(R.string.delete_page_alert))
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mActualLevel.removeBoardAtIndex(mActualIndex);

                                refreshView();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .setCancelable(false)
                        .create()
                        .show();


            }
        });

        mCenterTrashNavigationButton.setVisibility(View.GONE);
        mCenterHomeButton.setVisibility(View.GONE);
        mCenterBackButton.setVisibility(View.GONE);


    }

    @Override
    public void onBackPressed() {

        IOGlobalConfiguration.isInSwapMode = false;

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
            refreshView();
            return;
        }

        super.onBackPressed();
    }

    /*
    * OnBoardFragmentInteractionListener
    * */

    @Override
    public void onRegisterActiveLevel(IOLevel level) {
        if (level != null) {
            mActualLevel = level;

            Log.d("test", "registered level:" + level.toString());

            updateNavigationItems();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayer != null)
            mPlayer.reset();
    }

    @Override
    public void onPageScrolled(int newIndex) {
        mActualIndex = newIndex;
        mActualLevel.setActiveIndex(newIndex);

        updateNavigationItems();

        Log.d("TAG", "level size" + mActualLevel.getLevelSize());
        Log.d("TAG", "level size" + mActiveConfig.getLevel().getLevelSize());

    }

    public void tapOnSpeakableButton(final IOSpeakableImageButton spkBtn, final Integer level) {
        if (IOGlobalConfiguration.isEditing) {

            if (IOGlobalConfiguration.isInSwapMode) {

/*
                IOSpeakableImageButton firstButton = IOGlobalConfiguration.swapStorage;


                int secondIndex = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().indexOf(spkBtn);

                List<IOSpeakableImageButton> secondList = mActiveConfig.getLevel().getBoardAtIndex(mActualIndex).getButtons();

                IOGlobalConfiguration.firstList.remove(firstButton);
                IOGlobalConfiguration.firstList.add(IOGlobalConfiguration.firstIndex, spkBtn);

                secondList.remove(spkBtn);
                secondList.add(secondIndex, firstButton);

                IOGlobalConfiguration.isInSwapMode = false;
                IOGlobalConfiguration.swapStorage = null;

                refreshView();
*/

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();

                View layoutView = inflater.inflate(R.layout.edit_fragment_menu, null);

/*
                builder.setTitle(getString(R.string.choose));
*/

                builder.setView(layoutView);

                final AlertDialog dialog = builder.create();

/*
                final Switch matrioskaSwitch = (Switch) layoutView.findViewById(R.id.editModeAlertToggleBoard);
*/
/*
                Button editPictoButton = (Button) layoutView.findViewById(R.id.editModeAlertActionPicture);
                final Button editBoardButton = (Button) layoutView.findViewById(R.id.editModeAlertActionBoard);
                final Button movePictoButton = (Button) layoutView.findViewById(R.id.editModeAlertActionMove);
                final Button deletePictoButton = (Button) layoutView.findViewById(R.id.editModeAlertActionDelete);
*/

                ViewGroup imageLayout = (ViewGroup) layoutView.findViewById(R.id.image_layout);
                ViewGroup treeLayout = (ViewGroup) layoutView.findViewById(R.id.tree_layout);
                ViewGroup swapLayout = (ViewGroup) layoutView.findViewById(R.id.swap_layout);
                ViewGroup deleteLayout = (ViewGroup) layoutView.findViewById(R.id.delete_layout);

                ImageView imageView = (ImageView) layoutView.findViewById(R.id.image_view);

                imageView.setImageDrawable(spkBtn.getDrawable());

/*
                matrioskaSwitch.setChecked(spkBtn.getIsMatrioska());
                editBoardButton.setEnabled(spkBtn.getIsMatrioska());
*/

/*
                matrioskaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        spkBtn.setIsMatrioska(isChecked);
                        editBoardButton.setEnabled(isChecked);
                    }
                });
*/

                imageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //spkBtn.showInsertDialog();
                        Intent cIntent = new Intent(getApplicationContext(), IOCreateButtonActivity.class);

                        cIntent.putExtra(BUTTON_INDEX, mActualLevel.getBoardAtIndex(mActualIndex).getButtons().indexOf(spkBtn));
                        cIntent.putExtra(BUTTON_TEXT, spkBtn.getSentence());
                        cIntent.putExtra(BUTTON_TITLE, spkBtn.getmTitle());
                        cIntent.putExtra(BUTTON_IMAGE_FILE, spkBtn.getmImageFile());
                        cIntent.putExtra(BUTTON_AUDIO_FILE, spkBtn.getAudioFile());
                        cIntent.putExtra(BUTTON_INTENT_NAME, spkBtn.getIntentName());
                        cIntent.putExtra(BUTTON_INTENT_PACKAGENAME, spkBtn.getIntentPackageName());

                        startActivityForResult(cIntent, CREATE_BUTTON_CODE);

                        dialog.dismiss();
                    }
                });

                treeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialog.Builder(IOBoardActivity.this)
                                .setTitle(getResources().getString(R.string.tree_board))
                                .setMessage(getString(R.string.tree_alert_text))
                                .setPositiveButton(getResources().getString(R.string.set), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        spkBtn.setIsMatrioska(true);

                                        IOLevel nestedBoard = spkBtn.getLevel();

                                        pushLevel(nestedBoard);
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), null)
                                .setNeutralButton(getString(R.string.disable), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        spkBtn.setIsMatrioska(false);
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();

                        dialog.dismiss();
                    }
                });

                swapLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

/*
                        dialog.dismiss();


                        new AlertDialog.Builder(IOBoardActivity.this)
                                .setTitle(getString(R.string.move))
                                .setMessage(getString(R.string.move_hint))
                                .setPositiveButton(getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        IOGlobalConfiguration.isInSwapMode = true;
                                        IOGlobalConfiguration.swapStorage = spkBtn;

                                        for (IOSpeakableImageButton btn : mActualLevel.getBoardAtIndex(mActualIndex).getButtons())
                                            if (btn.getmSentence().equals(IOGlobalConfiguration.swapStorage.getmSentence()) && btn.getmImageFile().equals(IOGlobalConfiguration.swapStorage.getmImageFile()))
                                                IOGlobalConfiguration.firstIndex = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().indexOf(btn);

                                        IOGlobalConfiguration.firstList = mActualLevel.getBoardAtIndex(mActualIndex).getButtons();
                                        IOGlobalConfiguration.swapLevelIndex = mActualIndex;
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), null)
                                .create()
                                .show();
*/

                        Toast.makeText(IOBoardActivity.this, getString(R.string.not_yet_implemented), Toast.LENGTH_LONG).show();

                    }
                });

                deleteLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dialog.dismiss();

                        new AlertDialog.Builder(IOBoardActivity.this)
                                .setTitle(getResources().getString(R.string.warning))
                                .setMessage(getString(R.string.delete_image_alert))
                                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        int index = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().indexOf(spkBtn);
                                        mActualLevel.getBoardAtIndex(mActualIndex).getButtons().remove(spkBtn);
                                        mActualLevel.getBoardAtIndex(mActualIndex).getButtons().add(index, new IOSpeakableImageButton(IOBoardActivity.this));

                                        refreshView();
                                    }
                                })
                                .setNegativeButton(getString(R.string.no), null)
                                .setCancelable(false)
                                .create()
                                .show();
                    }
                });

                dialog.show();
            }
        } else {
            {

                if (spkBtn.getAudioFile() != null && spkBtn.getAudioFile().length() > 0) {

                    try {

                        if (mPlayingFile != null && mPlayingFile.equals(spkBtn.getAudioFile())) {
                            mPlayer.reset();
                            mPlayingFile = null;
                        } else {
                            mPlayer.reset();
                            mPlayer.setDataSource(spkBtn.getAudioFile());
                            mPlayer.prepare();
                            mPlayingFile = spkBtn.getAudioFile();
                            mPlayer.start();

                        }


                    } catch (IOException e) {
                        Log.e("playback_debug", "prepare() failed");
                    }
                } else if (spkBtn.getVideoFile() != null && spkBtn.getVideoFile().length() > 0) {

                    Intent intent = new Intent(this, IOVideoPlayerActivity.class);
                    intent.putExtra(IOVideoPlayerActivity.VIDEO_URL, spkBtn.getVideoFile());

                    startActivity(intent);

                } else if (mCanSpeak) {
                    Log.d("speakable_debug", "should say: " + spkBtn.getSentence());
                    tts.speak(spkBtn.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.tts_notinitialized), Toast.LENGTH_LONG).show();
                }

                if (spkBtn.getIsMatrioska() && null != spkBtn.getLevel()) {
                    pushLevel(spkBtn.getLevel());
                }

                if (spkBtn.getIntentName() != null && spkBtn.getIntentName().length() > 0) {
                    Intent intent = new Intent();
                    intent.setClassName(spkBtn.getIntentPackageName(),
                            spkBtn.getIntentName());
                    startActivity(intent);

                }
            }
        }
    }


    @Override
    public void onLevelConfigurationChanged() {
        refreshView();
    }

    @Override
    protected void onResume() {
/*
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
*/
        refreshView();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mUnlockAlert != null && mUnlockAlert.isShowing())
            mUnlockAlert.dismiss();
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
        menu.findItem(R.id.scanMode).setChecked(IOGlobalConfiguration.isScanMode);

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {

        IOGlobalConfiguration.isInSwapMode = false;

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

                Integer rows = mActualLevel.getBoardAtIndex(mActualIndex).getRows();
                Integer columns = mActualLevel.getBoardAtIndex(mActualIndex).getCols();

                final CheckBox bordersCheckbox = (CheckBox) layoutView.findViewById(R.id.bordersCheckbox);
                final CheckBox swipeCheckbox = (CheckBox) layoutView.findViewById(R.id.swipe_checkbox);
                final CheckBox bigNavCheckbox = (CheckBox) layoutView.findViewById(R.id.bignav_checkbox);
		        final CheckBox labelsCheckbox = (CheckBox) layoutView.findViewById(R.id.label_checkbox);

                bordersCheckbox.setChecked(IOConfiguration.getShowBorders());
                swipeCheckbox.setChecked(IOConfiguration.isSwipeEnabled());
                bigNavCheckbox.setChecked(IOConfiguration.isBigNavigation());
		        labelsCheckbox.setChecked(IOConfiguration.isShowLabels());

                builder.setTitle(getResources().getString(R.string.settings))
                        .setView(layoutView)
                        .setPositiveButton(getResources().getString(R.string.apply), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (newCols == 0)
                                    newCols++;
                                if (newRows == 0)
                                    newRows++;

                                mActualLevel.getBoardAtIndex(mActualIndex).setCols(newCols);
                                mActualLevel.getBoardAtIndex(mActualIndex).setRows(newRows);

                                IOConfiguration.setShowBorders(bordersCheckbox.isChecked());
                                IOConfiguration.setSwipeEnabled(swipeCheckbox.isChecked());
                                IOConfiguration.setBigNavigation(bigNavCheckbox.isChecked());
                                IOConfiguration.setShowLabels(labelsCheckbox.isChecked());

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
                toggleEditing();

                break;
            }

            case R.id.scanMode: {
                Log.d("options menu", "scan mode selected");
                item.setChecked(!item.isChecked());
                IOGlobalConfiguration.isScanMode = item.isChecked();

                updateNavigationItems();

                if (IOGlobalConfiguration.isScanMode)
                    startScanMode();
                else
                    stopScanMode();

                break;
            }

            case R.id.action_new: {
                newBoard();
                break;
            }

            case R.id.action_save: {
                saveBoard(null);
                break;
            }

            case R.id.action_save_as: {
                saveBoardAs();
                break;
            }

            case R.id.action_load: {
                loadBoard();
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
                if (IOGlobalConfiguration.isEditing)
                    toggleEditing();
                lockUI();
                break;
            }

            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method used to create a new configuration file.
     */
    private void newBoard() {

        if (IOHelper.checkForRequiredPermissions(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.warning))
                    .setMessage(getString(R.string.new_board_alert))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final AlertDialog.Builder alert = new AlertDialog.Builder(IOBoardActivity.this);

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

                                        FragmentManager fm = getSupportFragmentManager();
                                        while (fm.getBackStackEntryCount() > 0)
                                            fm.popBackStackImmediate();

                                        mActiveConfig = new IOConfiguration();
                                        fm.beginTransaction()
                                                .replace(mFrameLayout.getId(), IOPaginatedBoardFragment.newInstance(mActiveConfig.getLevel()))
                                                .commit();

                                        if (!IOGlobalConfiguration.isEditing)
                                            toggleEditing();


                                        saveBoard(value);
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


                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                    .setCancelable(false)
                    .create()
                    .show();
        }
    }

    /**
     * Unique method to save a board.
     * Please use no other methods.
     */
    private void saveBoard(String fileName) {
        if (IOHelper.checkForRequiredPermissions(this)) {

            boolean result;

            if (null != fileName)
                result = mActiveConfig.save(fileName);
            else if (null != mActualConfigName)
                result = mActiveConfig.save(mActualConfigName);
            else
                result = mActiveConfig.save();

            if (result == false) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.xml_save_fail))
                        .setNegativeButton(getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Method used to ask a filename to the user.
     */
    private void saveBoardAs() {

        if (IOHelper.checkForRequiredPermissions(this)) {

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
                        saveBoard(value);
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
        }
    }

    private void loadBoard() {
        if (IOHelper.checkForRequiredPermissions(this)) {
            File dirFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
            if (!dirFile.exists())
                dirFile.mkdirs();

            final String[] configFiles = dirFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {

                    if (filename.indexOf(".xml") != -1) {
                        return true;
                    }

                    return false;
                }
            });

            int ss = configFiles.length;
            for (int i = 0; i < ss; i++) {
                configFiles[i] = configFiles[i].replace(".xml", "");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);

            adapter.addAll(configFiles);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.choose))
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("media_debug", "click on item " + which);

                            String fileName = configFiles[which] + ".xml";
                            Log.d("board_debug", fileName);

                            FragmentManager fm = getSupportFragmentManager();
                            while (fm.getBackStackEntryCount() > 0)
                                fm.popBackStackImmediate();


                            mActiveConfig = IOConfiguration.getSavedConfiguration(fileName);

                            if (mActiveConfig != null && mActiveConfig.getLevel() != null) {

                                SharedPreferences preferences = getSharedPreferences(IOApplication.APPLICATION_NAME, MODE_PRIVATE);

                                mActualConfigName = preferences.getString(IOGlobalConfiguration.IO_LAST_BOARD_USED, null);
                                if (mActualConfigName != null)
                                    mActualConfigName = mActualConfigName.replace(".xml", "");

                                fm.beginTransaction()
                                        .replace(mFrameLayout.getId(), IOPaginatedBoardFragment.newInstance(mActiveConfig.getLevel()))
                                        .commit();
                            }
                        }
                    }).setNegativeButton(getResources().getString(R.string.cancel), null)
                    .create().show();
        }
    }

    private void toggleEditing() {
        IOGlobalConfiguration.isEditing = !IOGlobalConfiguration.isEditing;


        if (!IOGlobalConfiguration.isEditing) {
            if (null == mActualConfigName)
                saveBoard(null);
            else
                saveBoard(mActualConfigName);

            mLeftEditButton.setVisibility(View.GONE);
            mRightEditButton.setVisibility(View.GONE);

            mCenterTrashNavigationButton.setVisibility(View.GONE);

            setTitle(getString(R.string.app_name));


        } else {

            mLeftEditButton.setVisibility(View.VISIBLE);
            mRightEditButton.setVisibility(View.VISIBLE);

            mCenterTrashNavigationButton.setVisibility(View.VISIBLE);

            setTitle(getString(R.string.edit_mode));

        }

        refreshView();
    }

    public boolean canGoLeft() {
        IOPaginatedBoardFragment fragment = (IOPaginatedBoardFragment) getSupportFragmentManager().findFragmentById(mFrameLayout.getId());
        return fragment.canGoLeft();
    }

    public boolean canGoRight() {
        IOPaginatedBoardFragment fragment = (IOPaginatedBoardFragment) getSupportFragmentManager().findFragmentById(mFrameLayout.getId());
        return fragment.canGoRight();

    }

    private void updateNavigationItems() {

        if (IOGlobalConfiguration.isScanMode || IOConfiguration.isBigNavigation()) {
            mLeftNavigationButton.setVisibility(View.INVISIBLE);
            mRightNavigationButton.setVisibility(View.INVISIBLE);
        } else
            hideSideNavButtons();


        if (canGoLeft()) {
            if (IOGlobalConfiguration.isScanMode || IOConfiguration.isBigNavigation()) {
                mLeftSideArrowButton.setVisibility(View.VISIBLE);
            } else {
                mLeftNavigationButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (IOGlobalConfiguration.isScanMode || IOConfiguration.isBigNavigation()) {
                mLeftSideArrowButton.setVisibility(View.INVISIBLE);
            } else {
                mLeftNavigationButton.setVisibility(View.INVISIBLE);
            }

        }

        if (canGoRight()) {
            if (IOGlobalConfiguration.isScanMode || IOConfiguration.isBigNavigation()) {
                mRightSideArrowButton.setVisibility(View.VISIBLE);
            } else {
                mRightNavigationButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (IOGlobalConfiguration.isScanMode || IOConfiguration.isBigNavigation()) {
                mRightSideArrowButton.setVisibility(View.INVISIBLE);
            } else {
                mRightNavigationButton.setVisibility(View.INVISIBLE);
            }
        }


    }

    private void paginateLeft() {
        IOPaginatedBoardFragment fragment = (IOPaginatedBoardFragment) getSupportFragmentManager().findFragmentById(mFrameLayout.getId());
        fragment.paginateLeft();

        updateNavigationItems();
    }

    private void paginateRight() {
        IOPaginatedBoardFragment fragment = (IOPaginatedBoardFragment) getSupportFragmentManager().findFragmentById(mFrameLayout.getId());
        fragment.paginateRight();

        updateNavigationItems();
    }

    private void pushLevel(IOLevel board) {

        Log.d("test", "pushing board " + board.toString());

        FragmentManager fm = getSupportFragmentManager();

        fm.beginTransaction()
                .replace(mFrameLayout.getId(), IOPaginatedBoardFragment.newInstance(board))
/*
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
*/
                .addToBackStack(null)
                .commit();

        mActualIndex = 0;

        mCenterBackButton.setVisibility(View.VISIBLE);
        mCenterHomeButton.setVisibility(View.VISIBLE);


    }

    private void refreshView() {
        refreshView(mActualLevel.getActiveIndex());
        updateNavigationItems();
    }

    private void refreshView(int index) {

        FragmentManager fm = getSupportFragmentManager();
        IOPaginatedBoardFragment fragment = (IOPaginatedBoardFragment) fm.findFragmentById(mFrameLayout.getId());

        fragment.refreshView(index);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            mCenterBackButton.setVisibility(View.VISIBLE);
            mCenterHomeButton.setVisibility(View.VISIBLE);
        } else {
            mCenterBackButton.setVisibility(View.GONE);
            mCenterHomeButton.setVisibility(View.GONE);

        }


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

        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        findViewById(R.id.rootContainer).invalidate();
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
                String videoFile = extras.getString(BUTTON_VIDEO_FILE);
                String intentName = extras.getString(BUTTON_INTENT_NAME);
                String intentPackageName = extras.getString(BUTTON_INTENT_PACKAGENAME);

                IOSpeakableImageButton button = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(index);

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

                button.setIntentName(intentName);

                button.setIntentPackageName(intentPackageName);

                button.setAudioFile(audioFile);
                button.setVideoFile(videoFile);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }


    private void lockUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (IOHelper.canGoImmersive())
                hideSystemUI();

            mUILocked = true;
        }
    }

    private void showUnlockAlert() {
        this.mUnlockAlert = new AlertDialog.Builder(IOBoardActivity.this)
                .setTitle(getResources().getString(R.string.unlock))
                .setMessage(getResources().getQuantityString(R.plurals.unlock_question, IOBoardActivity.this.mUnlockTimeout.intValue(), IOBoardActivity.this.mUnlockTimeout.intValue()))
                .setPositiveButton(getResources().getString(R.string.unlock), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUILocked = false;
                        showSystemUI();
                        mDecorView.invalidate();
                        if (IOHelper.canGoImmersive() == false)
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

                        if (null != mUnlockAlert)
                            mUnlockAlert.setMessage(getResources().getQuantityString(R.plurals.unlock_question, sVal, sVal));
                    }

                    @Override
                    public void onFinish() {
                        if (mUnlockAlert != null && mUnlockAlert.isShowing())
                            mUnlockAlert.dismiss();
                        lockUI();
                    }
                };

                IOBoardActivity.this.mUnlockCountDown.start();
            }
        });

        this.mUnlockAlert.show();
    }


    private void showHintAlert() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.welcome))
                .setMessage(getString(R.string.welcome_text))
                .setPositiveButton(getResources().getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUILocked = false;
                        toggleEditing();
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

        if (IOGlobalConfiguration.isEditing)
            toggleEditing();

        /*
        * Neurosky Mindwave support
        * */
/*
        if (tgDevice != null)
            tgDevice.connect(true);
*/

        View scanClickDetector = findViewById(R.id.scanModeClickDetector);

        scanClickDetector.setVisibility(View.VISIBLE);

        scanClickDetector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mActualScanIndex == mScanModeMaxIndex)
                    paginateRight();
                else if (mActualScanIndex == mScanModeMaxIndex + 1)
                    paginateLeft();
                else {
                    IOSpeakableImageButton actualButton = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(mActualScanIndex);
                    actualButton.callOnClick();
                }

            }
        });

        scanClickDetector.bringToFront();

/*
        showSideNavButtons();
*/

        scanModeHandler = new Handler();

        mActualScanIndex = 0;
        highlightButtonAtIndex(mActualScanIndex);
        scanModeRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Debug", "higlighting: " + (mActualScanIndex + 1));
                highlightButtonAtIndex(mActualScanIndex + 1);

                scanModeHandler.postDelayed(this, mScanModeDelay);

            }
        };

        scanModeHandler.postDelayed(scanModeRunnable, mScanModeDelay);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_F1 && IOGlobalConfiguration.isScanMode) {
            if (mActualScanIndex == mScanModeMaxIndex)
                paginateRight();
            else if (mActualScanIndex == mScanModeMaxIndex + 1)
                paginateLeft();
            else {
                IOSpeakableImageButton actualButton = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(mActualScanIndex);
                actualButton.callOnClick();
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Actually not more used.
     * TODO: implement permissions callbacks for most sensible actions(eg. saving, loading...)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0)
            return;

        switch (requestCode) {

            case IOHelper.IO_PERMISSIONS_READ_STORAGE_FOR_LOADING:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadBoard();
                break;
            case IOHelper.IO_PERMISSIONS_WRITE_STORAGE_FOR_SAVING:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveBoard(null);
                break;
            case IOHelper.IO_PERMISSIONS_WRITE_STORAGE_FOR_SAVINGAS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveBoardAs();
                break;

            default:
                break;
        }
    }

    private void highlightButtonAtIndex(int index) {

        List<IOSpeakableImageButton> buttons = mActualLevel.getBoardAtIndex(mActualIndex).getButtons();
        mScanModeMaxIndex = mActualLevel.getBoardAtIndex(mActualIndex).getCols() * mActualLevel.getBoardAtIndex(mActualIndex).getRows();

        index = IOHelper.mod(index, mScanModeMaxIndex + 2);

        if (index == mScanModeMaxIndex + 1 && mLeftSideArrowButton.getVisibility() == View.INVISIBLE) {
            index += 1;
            index = IOHelper.mod(index, mScanModeMaxIndex + 2);
        }

        if (index == mScanModeMaxIndex && mRightSideArrowButton.getVisibility() == View.INVISIBLE) {
            index += 1;
            index = IOHelper.mod(index, mScanModeMaxIndex + 2);
        }

        int originalIndex = index;

        for (int i = index; i < mScanModeMaxIndex; i++) {

            if (i < buttons.size() && buttons.get(i).getmImageFile() != null && buttons.get(i).getmImageFile().length() > 0) {
                break;
            }
            index = i + 1;
        }

        if (index == mScanModeMaxIndex + 1 && mLeftSideArrowButton.getVisibility() == View.INVISIBLE) {
            index += 1;
            index = IOHelper.mod(index, mScanModeMaxIndex + 2);
        }

        if (index == mScanModeMaxIndex && mRightSideArrowButton.getVisibility() == View.INVISIBLE) {
            index += 1;
            index = IOHelper.mod(index, mScanModeMaxIndex + 2);
        }

        Log.d("debug", "calculated index: " + index);

        int scanModePrevIndex = Math.min(originalIndex - 1, mScanModeMaxIndex);

        if (scanModePrevIndex < mScanModeMaxIndex) {
            for (int i = scanModePrevIndex; i >= 0; i--) {
                if (i < buttons.size() && buttons.get(i).getmImageFile() != null && buttons.get(i).getmImageFile().length() > 0) {
                    scanModePrevIndex = i;
                    break;
                }

                scanModePrevIndex = i - 1;
            }
        }

        scanModePrevIndex = IOHelper.mod(scanModePrevIndex, mScanModeMaxIndex + 2);


        if (index == mScanModeMaxIndex + 1) {
            //left

            mLeftSideArrowButton.setIsHiglighted(true);
            mRightSideArrowButton.setIsHiglighted(false);
            mRightSideArrowButton.invalidate();
            mLeftSideArrowButton.invalidate();

            if (scanModePrevIndex < mScanModeMaxIndex && scanModePrevIndex < mActualLevel.getBoardAtIndex(mActualIndex).getButtons().size()) {
                IOSpeakableImageButton prevbutton = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(scanModePrevIndex);
                prevbutton.setIsHiglighted(false);
                prevbutton.invalidate();
            }

        } else if (index == mScanModeMaxIndex) {
            //right
            mRightSideArrowButton.setIsHiglighted(true);
            mRightSideArrowButton.invalidate();

            if (scanModePrevIndex < mScanModeMaxIndex && scanModePrevIndex < buttons.size()) {
                buttons.get(scanModePrevIndex).setIsHiglighted(false);
                buttons.get(scanModePrevIndex).invalidate();
            }
        } else {

            mRightSideArrowButton.setIsHiglighted(false);
            mRightSideArrowButton.invalidate();

            mLeftSideArrowButton.setIsHiglighted(false);
            mLeftSideArrowButton.invalidate();

            if (index < mActualLevel.getBoardAtIndex(mActualIndex).getButtons().size()) {
                IOSpeakableImageButton button = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(index);
                button.setIsHiglighted(true);
                button.invalidate();
            }

            if (scanModePrevIndex <= mScanModeMaxIndex && scanModePrevIndex < mActualLevel.getBoardAtIndex(mActualIndex).getButtons().size()) {
                IOSpeakableImageButton prevbutton = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(scanModePrevIndex);
                prevbutton.setIsHiglighted(false);
                prevbutton.invalidate();
            }
        }

        mActualScanIndex = index;

    }


    private void stopScanMode() {

        IOGlobalConfiguration.isScanMode = false;
        int scanModeMaxIndex = mActualLevel.getBoardAtIndex(mActualIndex).getCols() * mActualLevel.getBoardAtIndex(mActualIndex).getRows();
        int index = IOHelper.mod(mActualScanIndex, scanModeMaxIndex);

        if (index < mActualLevel.getBoardAtIndex(mActualIndex).getButtons().size()) {
            IOSpeakableImageButton button = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(index);
            button.setIsHiglighted(false);
            button.invalidate();
        }

        View scanClickDetector = findViewById(R.id.scanModeClickDetector);

        scanClickDetector.setVisibility(View.GONE);
        scanClickDetector.setOnClickListener(null);

        if (scanModeHandler != null)
            scanModeHandler.removeCallbacks(scanModeRunnable);
        scanModeHandler = null;
        scanModeRunnable = null;
/*
        tgDevice.close();
*/

        updateNavigationItems();
        refreshView();
    }



/*
* Neurosky Mindwave support
* */

/*
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
*/
/*
                    Log.v("HelloEEG", "PoorSignal: " + msg.arg1);
*//*

                    break;
                case TGDevice.MSG_ATTENTION:
*/
/*
                    Log.v("HelloEEG", "Attention: " + msg.arg1);
*//*

                    break;
                case TGDevice.MSG_RAW_DATA:
                    int rawValue = msg.arg1;
                    break;
                case TGDevice.MSG_EEG_POWER:
                    TGEegPower ep = (TGEegPower) msg.obj;
*/
/*
                    Log.v("HelloEEG", "Delta: " + ep.delta);
*//*

                    break;
                case TGDevice.MSG_BLINK:
                    Toast.makeText(getApplicationContext(), "blink!", Toast.LENGTH_SHORT).show();
                    Log.v("HelloEEG", "blink!: " + msg.arg1);
                    IOSpeakableImageButton actualButton = mActualLevel.getBoardAtIndex(mActualIndex).getButtons().get(mActualScanIndex);
                    actualButton.callOnClick();


                default:
                    break;
            }
        }
    };
*/
}
