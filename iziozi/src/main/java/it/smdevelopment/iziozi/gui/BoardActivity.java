package it.smdevelopment.iziozi.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.smdevelopment.iziozi.R;
import it.smdevelopment.iziozi.core.SMIziOziConfiguration;
import it.smdevelopment.iziozi.core.SpeakableImageButton;


public class BoardActivity extends Activity {

    private TextToSpeech tts;
    private Boolean mIsEditing = false, mCanSpeak = false;
    private List<LinearLayout> homeRows = new ArrayList<LinearLayout>();
    private SMIziOziConfiguration mConfig;

    private View mDecorView;


    int newRows, newCols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDecorView= getWindow().getDecorView();

        hideSystemUI();


        this.mConfig = SMIziOziConfiguration.getSavedConfiguration();

        if(this.mConfig == null)
            this.mConfig = new SMIziOziConfiguration(this);
        else
            this.mConfig.setContext(this);

        createView();

        this.tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.speak("Sono pronta a parlare per te!", TextToSpeech.QUEUE_FLUSH, null);
                mCanSpeak = true;
            }
        });

    }


    private void createView() {
        setContentView(buildView());
    }

    private View buildView() {

        this.homeRows.clear();
        final List<SpeakableImageButton> mButtons = new ArrayList<SpeakableImageButton>();
        List<SpeakableImageButton> configButtons = this.mConfig.getButtons();

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
            rowLayout.setBackgroundColor(Color.argb(255, color.nextInt(255), color.nextInt(255), color.nextInt(255)));
            mainLayout.addView(rowLayout);
            this.homeRows.add(rowLayout);
            Log.d("home debug", "row created");
        }

        for (int j = 0; j < this.homeRows.size() ; j++) {
            LinearLayout homeRow = this.homeRows.get(j);

            for (int i = 0; i < this.mConfig.getCols(); i++) {
                LinearLayout btnContainer = new LinearLayout(this);
                LayoutParams btnContainerParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.f);
                btnContainer.setLayoutParams(btnContainerParams);
                btnContainer.setOrientation(LinearLayout.VERTICAL);
                Random color = new Random();
                btnContainer.setBackgroundColor(Color.argb(255, color.nextInt(255), color.nextInt(255), color.nextInt(255)));
                homeRow.addView(btnContainer);
                Log.d("homedebug", "container created");

                SpeakableImageButton imgButton = configButtons.size() > 0 ? configButtons.get(mButtons.size()) : new SpeakableImageButton(this);
                imgButton.setmContext(this);

                imgButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                imgButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

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

        this.mConfig.setButtons(mButtons);

        return mainLayout;
    }

    private void tapOnSpeakableButton(SpeakableImageButton spkBtn) {
        if (mIsEditing) {
//            spkBtn.showInsertDialog();
            Intent cIntent = new Intent(getApplicationContext(), CreateButtonActivity.class);
            startActivity(cIntent);
        } else {
            Log.d("speakable_debug", "tap on speak");
            if (mCanSpeak) {
                Log.d("speakable_debug", "should say: " + spkBtn.getSentence());
                if (spkBtn.getSentence() == "")
                    tts.speak("Questo pulsante non ha una frase associata!", TextToSpeech.QUEUE_FLUSH, null);
                else
                    tts.speak(spkBtn.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Toast.makeText(this, "TTS not yet initialized!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Log.d("menu_debug", "menu created!");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings: {
                Log.d("home debug", "options selected");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();

                View layoutView = inflater.inflate(R.layout.settings_layout, null);

                Integer rows = this.mConfig.getRows();
                Integer columns = this.mConfig.getCols();

                builder.setTitle("Settings")
                        .setView(layoutView)
                        .setPositiveButton(getResources().getString(R.string.apply), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("dialog", "should dismiss and apply");

                                if (newCols == 0)
                                    newCols++;
                                if (newRows == 0)
                                    newRows++;

                                BoardActivity.this.mConfig.setCols(newCols);
                                BoardActivity.this.mConfig.setRows(newRows);

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

                if(!mIsEditing)
                    this.mConfig.save();

                break;
            }

            case R.id.action_save:
            {
                this.mConfig.save();
                break;
            }

            default: break;

        }

        return super.onOptionsItemSelected(item);
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            hideSystemUI();
    }
}
