package it.iziozi.iziozi.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApiClient;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.dbclasses.IOPictogram;


public class SpeechBoardActivity extends AppCompatActivity implements View.OnClickListener {


    public static final int SPEECH_RECOGNIZER_CODE = 1;

    public static final String IMAGES_RESULT_KEY = "downloaded_images";
    public static final String IMAGE_FILE = "image_file";
    public static final String IMAGE_URL = "image_url";
    public static final String IMAGE_TITLE = "image_title";

    AsyncHttpClient client;

    ArrayList<String> wordsFromUser;
    ArrayList<HashMap<String, String>> downloadedImagesInfo;

    int numberOfWords, currentDownloadNum, totalToDownload;

    ProgressBar progressDownload;
    TextView tvDownloadInfo;
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_board_activity);

        downloadedImagesInfo = new ArrayList<>();

        client = new AsyncHttpClient();
        wordsFromUser = null;

        btnDone = (Button) findViewById(R.id.btnSpeechDone);
        tvDownloadInfo = (TextView) findViewById(R.id.tvProgressInfo);
        progressDownload = (ProgressBar) findViewById(R.id.speechBoardProgress);
        progressDownload.setIndeterminate(true);

        Intent intent = getIntent();
        if (intent != null) {
            wordsFromUser = intent.getStringArrayListExtra("speechWords");
            numberOfWords = wordsFromUser.size();

            // Create the views dynamically
            prepareView(wordsFromUser);

            tvDownloadInfo.setVisibility(View.GONE);
            progressDownload.setVisibility(View.GONE);

            btnDone.setOnClickListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnSpeechDone:
                setTitle(getString(R.string.get_images));
                LinearLayout layout = (LinearLayout) findViewById(R.id.speechBoardWordsLayout);
                layout.setVisibility(View.GONE);
                btnDone.setVisibility(View.GONE);

                tvDownloadInfo.setVisibility(View.VISIBLE);
                progressDownload.setVisibility(View.VISIBLE);

                wordsFromUser.clear();
                getEditTextInput(layout, wordsFromUser);

                totalToDownload = wordsFromUser.size();
                tvDownloadInfo.setText(getString(R.string.downloading) + currentDownloadNum
                        + "/" + totalToDownload);
                remoteSearchForImages(wordsFromUser);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SPEECH_RECOGNIZER_CODE) {
            // save pictogram
            HashMap<String, String> res = new HashMap<>();

            String imageFile = data.getStringExtra(IMAGE_FILE);
            String imageUrl = data.getStringExtra(IMAGE_URL);
            String imageTitle = data.getStringExtra(IMAGE_TITLE);

            res.put(IMAGE_FILE, imageFile);
            res.put(IMAGE_TITLE, imageTitle);
            res.put(IMAGE_URL, imageUrl);

            downloadedImagesInfo.add(res);

            if (wordsFromUser.size() > 0) {
                remoteSearchForImages(wordsFromUser);
            } else {
                // Go back to previous activity
                checkDownloadedImagesAndFinish();
            }

        } else if (resultCode == RESULT_CANCELED) {
            // check to see if there are more images to download
            remoteSearchForImages(wordsFromUser);
        }
    }

    /**
     * Prepares the view from the first parameter which is a series of edittexts in a two column
     * fashion.
     * @param wordsFromUser
     * @return the view created
     */
    private LinearLayout prepareView(ArrayList<String> wordsFromUser) {
        LinearLayout topContainer = (LinearLayout) findViewById(R.id.speechBoardWordsLayout);
        LinearLayout firstCol = new LinearLayout(this);
        LinearLayout secondCol = new LinearLayout(this);

        topContainer.setLayoutParams(new LinearLayout.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        topContainer.setOrientation(LinearLayout.HORIZONTAL);
        topContainer.addView(firstCol);
        topContainer.addView(secondCol);

        firstCol.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
        firstCol.setOrientation(LinearLayout.VERTICAL);

        secondCol.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
        secondCol.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < wordsFromUser.size(); i++) {
            EditText editText = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            editText.setLayoutParams(params);

            editText.setGravity(Gravity.CENTER);
            editText.setText(wordsFromUser.get(i).toString());

            if (i % 2 == 0) firstCol.addView(editText);
            else secondCol.addView(editText);
        }

        return topContainer;
    }

    /**
     * This gets each word from each edittext box and stores it in the second parameter.
     * @param v
     * @param wordsFromUser
     */
    private void getEditTextInput(ViewGroup v, ArrayList<String> wordsFromUser) {

        for (int i = 0; i < v.getChildCount(); i++) {
            Object child = v.getChildAt(i);

            if (child instanceof EditText) {
                EditText editText = (EditText) child;

                if (!editText.getText().toString().equals("")) {
                    wordsFromUser.add(editText.getText().toString());
                }

            } else if (child instanceof ViewGroup) {
                getEditTextInput((ViewGroup) child, wordsFromUser);
            }
        }
    }

    /**
     * Checks to see if any images were downloaded, if they were it sets up a Bundle, otherwise it
     * informs the user that the board will not be created.
     */
    private void checkDownloadedImagesAndFinish() {
        if (downloadedImagesInfo.size() == 0) {

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.speech_board_empty_images_title))
                    .setMessage(getString(R.string.speech_board_empty_images))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            setResult(Activity.RESULT_CANCELED, intent);
                            finish();
                        }
                    })
                    .create().show();

        } else {
            Intent intent = new Intent();
            Bundle b = new Bundle();
            b.putSerializable(IMAGES_RESULT_KEY, downloadedImagesInfo);

            intent.putExtras(b);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Starts a remote search for a word. If it finds just one it downloads it, otherwise it starts
     * a new activity.
     * @param words
     */
    private void remoteSearchForImages(ArrayList<String> words) {
        if (words.size() == 0) checkDownloadedImagesAndFinish();

        else {
            RequestParams params = new RequestParams();

            final String query = words.remove(0);
            currentDownloadNum++;
            tvDownloadInfo.setText(getString(R.string.downloading) + currentDownloadNum
                    + "/" + totalToDownload);

            params.put("q", query);
            params.put("lang", Locale.getDefault().getLanguage());

            IOApiClient.get("pictures", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    super.onSuccess(statusCode, headers, response);

                    // correct response
                    int iter = response.length();

                    if (iter == 0) {
                        // no images
                        Toast.makeText(getApplicationContext(), getString(R.string.no_images), Toast.LENGTH_SHORT).show();
                        remoteSearchForImages(wordsFromUser);

                    } else if (iter == 1) {
                        // just one image, download it
                        try {
                            JSONObject jsonObject = response.getJSONObject(0);

                            IOPictogram pictogram = new IOPictogram();

                            pictogram.setId(jsonObject.getInt("id"));
                            pictogram.setFilePath(jsonObject.getString("file"));
                            pictogram.setUrl(jsonObject.getString("deepurl"));
                            String text = jsonObject.getString("text");
                            pictogram.setDescription(text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase());

                            File baseFolder = new File(Environment.getExternalStorageDirectory() + "/" + IOApplication.APPLICATION_FOLDER + "/pictograms");
                            Character pictoChar = pictogram.getFilePath().charAt(0);
                            File pictoFile = new File(baseFolder + "/" + pictoChar + "/" + pictogram.getFilePath());

                            client.get(pictogram.getUrl(), new FileAsyncHandler(pictoFile, pictogram));

                        } catch (JSONException e) {
                            Log.d("IziOzi", e.toString());
                        }

                    } else {
                        Intent intent = new Intent(getApplicationContext(), IORemoteImageSearchActivity.class);
                        intent.setAction(Intent.ACTION_SEARCH);
                        intent.putExtra(SearchManager.QUERY, query);
                        intent.putExtra(SpeechBoardActivity.class.getSimpleName(), true);

                        startActivityForResult(intent, SPEECH_RECOGNIZER_CODE);
                    }
                }
            });
        }
    }

    private class FileAsyncHandler extends FileAsyncHttpResponseHandler {

        File f;
        IOPictogram pictogram;

        public FileAsyncHandler(File file, IOPictogram pictogram) {
            super(file);
            this.f = file;
            this.pictogram = pictogram;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
            Toast.makeText(getApplicationContext(), getString(R.string.download_error), Toast.LENGTH_LONG).show();
            remoteSearchForImages(wordsFromUser);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, File file) {
            if (f.exists()) {
                // save pictogram
                HashMap<String, String> data = new HashMap<>();
                data.put(IMAGE_URL, pictogram.getUrl());
                data.put(IMAGE_TITLE, pictogram.getDescription());
                data.put(IMAGE_FILE, f.toString());

                downloadedImagesInfo.add(data);

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
            }

            remoteSearchForImages(wordsFromUser);
        }
    }
}
