package it.smdevelopment.iziozi;

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




public class HomeActivity extends Activity {

    private TextToSpeech tts;
    private Boolean mIsEditing = false, mCanSpeak = false;
    private List<LinearLayout> homeRows = new ArrayList<LinearLayout>();
    private List<SpeakableImageButton> homeBtns = new ArrayList<SpeakableImageButton>();

    int rows = 3, columns = 4;

    int newRows, newCols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createView();
        Log.d("cacca", "mucca");
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

    private View buildView()
    {

        this.homeBtns.clear();
        this.homeRows.clear();

        LinearLayout mainLayout = new LinearLayout(this);
        LayoutParams mainParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mainLayout.setLayoutParams(mainParams);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < rows ; i++) {

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

        for (LinearLayout homeRow : this.homeRows) {
            for (int i = 0; i < columns; i++) {
                LinearLayout btnContainer = new LinearLayout(this);
                LayoutParams btnContainerParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.f);
                btnContainer.setLayoutParams(btnContainerParams);
                btnContainer.setOrientation(LinearLayout.VERTICAL);
                Random color = new Random();
                btnContainer.setBackgroundColor(Color.argb(255, color.nextInt(255), color.nextInt(255), color.nextInt(255)));
                homeRow.addView(btnContainer);
                Log.d("homedebug", "container created");

                SpeakableImageButton imgButton = new SpeakableImageButton(this);
                imgButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                imgButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

                btnContainer.addView(imgButton);
                this.homeBtns.add(imgButton);

                imgButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int index = homeBtns.indexOf(v);
                        tapOnSpeakableButton(homeBtns.get(index));
                        //						Toast.makeText(getApplicationContext(), "Click on button "+index, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        return mainLayout;
    }

    private void tapOnSpeakableButton(SpeakableImageButton spkBtn) {
        if(mIsEditing)
        {
//			spkBtn.showInsertDialog();
            Intent cIntent = new Intent(getApplicationContext(), CreateButtonActivity.class);
            startActivity(cIntent);
        }else
        {
            Log.d("speakable_debug", "tap on speak");
            if(mCanSpeak)
            {
                Log.d("speakable_debug", "should say: "+spkBtn.getSentence());
                if(spkBtn.getSentence() == null)
                    tts.speak("Questo pulsante non ha una frase associata!", TextToSpeech.QUEUE_FLUSH, null);
                else
                    tts.speak(spkBtn.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
            }else
            {
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
        if(item.getItemId() == R.id.action_settings)
        {
            Log.d("home debug",	"options selected");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();

            View layoutView = inflater.inflate(R.layout.settings_layout, null);

            builder.setTitle("Settings")
                    .setView(layoutView)
                    .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("dialog", "should dismiss and apply");
                            columns = newCols;
                            rows = newRows;
                            createView();
                        }
                    })
                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("dialog", "should dismiss and discard");
                        }
                    });

            SeekBar sRows = (SeekBar) layoutView.findViewById(R.id.seekRows);
            SeekBar sCols = (SeekBar) layoutView.findViewById(R.id.seekCols);

            final TextView rowsLbl = (TextView) layoutView.findViewById(R.id.numRowsLbl);
            final TextView colsLbl = (TextView) layoutView.findViewById(R.id.numColsLbl);

            sRows.setProgress(rows);
            sCols.setProgress(columns);

            rowsLbl.setText(""+rows);
            colsLbl.setText(""+columns);

            sRows.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    Log.d("seeking", "seek rows "+ progress);
                    newRows = progress;
                    rowsLbl.setText(""+progress);
                }
            });

            sCols.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    Log.d("seeking", "seek cols "+ progress);
                    newCols = progress;
                    colsLbl.setText(""+progress);
                }
            });

            builder.create().show();
        }else if(item.getItemId() == R.id.editMode)
        {
            Log.d("options menu", "edit mode selected");
            item.setChecked(!item.isChecked());
            mIsEditing = item.isChecked();
        }
        return super.onOptionsItemSelected(item);
    }
}
