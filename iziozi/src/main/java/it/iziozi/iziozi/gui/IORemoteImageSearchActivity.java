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
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApiClient;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.dbclasses.IOPictogram;
import it.iziozi.iziozi.helpers.IOHelper;

public class IORemoteImageSearchActivity extends AppCompatActivity {

    private GridView mGridView;
    private List<IOPictogram> mPictograms;

    private TextView mEmptyTextView;

    private ProgressDialog mBarProgressDialog;
    private ProgressDialog mRingProgressDialog;

    boolean isSpeechBoardSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smimages_search);

        if (!IOHelper.checkForRequiredPermissions(this)) {
            finish();
        }

        mGridView = (GridView) findViewById(R.id.ImageSearchGridView);
        mEmptyTextView = (TextView) findViewById(R.id.ImageSearchNotFoundText);
        mEmptyTextView.setText(getString(R.string.no_picto_found));
        mEmptyTextView.setVisibility(View.INVISIBLE);

/*
        mGridView.setNumColumns(5);
*/
        mGridView.setAdapter(new RemoteImagesGridAdapter(this, R.layout.image_grid_cell));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (IOHelper.checkForRequiredPermissions(IORemoteImageSearchActivity.this) && isExternalStorageReadable()) {
                    final IOPictogram pictogram = mPictograms.get(position);

                    File baseFolder = new File(Environment.getExternalStorageDirectory() + "/" + IOApplication.APPLICATION_FOLDER + "/pictograms");
                    final Character pictoChar = pictogram.getFilePath().charAt(0);
                    File pictoFolder = new File(baseFolder + "/" + pictoChar + "/");
                    final File pictoFile = new File(baseFolder + "/" + pictoChar + "/" + pictogram.getFilePath());

                    final Intent backIntent;
                    if (isSpeechBoardSearch) {
                        backIntent = new Intent();
                        backIntent.putExtra(SpeechBoardActivity.IMAGE_URL, pictogram.getUrl());
                        backIntent.putExtra(SpeechBoardActivity.IMAGE_TITLE, pictogram.getDescription());
                    }
                    else {
                        backIntent = new Intent(getApplicationContext(), IOCreateButtonActivity.class);
                        backIntent.putExtra(IOCreateButtonActivity.IMAGE_URL, pictogram.getUrl());
                        backIntent.putExtra(IOCreateButtonActivity.IMAGE_TITLE, pictogram.getDescription());
                    }

                    if (!pictoFile.exists() && isExternalStorageWritable()) {

                        pictoFolder.mkdirs();

                        //download it

                        mBarProgressDialog = new ProgressDialog(IORemoteImageSearchActivity.this);
                        mBarProgressDialog.setTitle(getString(R.string.image_download_title));
                        mBarProgressDialog.setMessage(getString(R.string.download_text));
                        mBarProgressDialog.setProgressStyle(mBarProgressDialog.STYLE_HORIZONTAL);
                        mBarProgressDialog.setProgress(0);
                        mBarProgressDialog.setMax(100);
                        mBarProgressDialog.show();


                        AsyncHttpClient client = new AsyncHttpClient();
                        client.get(pictogram.getUrl(), new FileAsyncHttpResponseHandler(pictoFile) {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                if (null != mBarProgressDialog)
                                    mBarProgressDialog.cancel();
                                Toast.makeText(getApplicationContext(), getString(R.string.download_error), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onProgress(int bytesWritten, int totalSize) {
                                super.onProgress(bytesWritten, totalSize);

                                int progress = bytesWritten / totalSize;
                                progress *= 100;

                                mBarProgressDialog.setProgress(progress);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File downloadedFile) {

                                if (pictoFile.exists()) {
                                    if (isSpeechBoardSearch) {
                                        backIntent.putExtra(SpeechBoardActivity.IMAGE_FILE, pictoFile.toString());
                                        setResult(Activity.RESULT_OK, backIntent);
                                        finish();
                                    }
                                    else {
                                        backIntent.putExtra(IOCreateButtonActivity.IMAGE_FILE, pictoFile.toString());
                                        finish();
                                        startActivity(backIntent);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.image_save_error), Toast.LENGTH_SHORT).show();
                                    mBarProgressDialog.cancel();
                                }
                            }
                        });
                    } else {
                        //file already exists
                        if (isSpeechBoardSearch) {
                            backIntent.putExtra(SpeechBoardActivity.IMAGE_FILE, pictoFile.toString());
                            setResult(Activity.RESULT_OK, backIntent);
                            finish();
                        }
                        else {
                            backIntent.putExtra(IOCreateButtonActivity.IMAGE_FILE, pictoFile.toString());
                            finish();
                            startActivity(backIntent);
                        }
                    }
                }
            }
        });

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.data_connection_needed))
                    .setNegativeButton(getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create()
                    .show();
        } else
            handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            isSpeechBoardSearch = intent.getBooleanExtra(SpeechBoardActivity.class.getSimpleName(), false);
            searchImages(query);
        }
    }

    private void searchImages(String queryString) {

        mRingProgressDialog = ProgressDialog.show(this, getString(R.string.please_wait), getString(R.string.search_in_progress), true);

        mPictograms = new ArrayList<IOPictogram>();

        SharedPreferences prefs = getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);

        prefs.getString(IOApplication.APPLICATION_LOCALE, Locale.getDefault().getLanguage());

        RequestParams params = new RequestParams();

        params.put("q", queryString);
        params.put("lang", Locale.getDefault().getLanguage());

        IOApiClient.get("pictures", params, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                Log.d("http debug", getRequestURI().toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                //something went wrong

                Toast.makeText(getApplicationContext(), getString(R.string.unexpected_response), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                super.onFinish();

                mRingProgressDialog.cancel();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                //Correct response

                int iter = response.length();

                if (iter == 0) {

                    mEmptyTextView.setVisibility(View.VISIBLE);

                } else {
                    for (int i = 0; i < iter; i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);

                            IOPictogram pictogram = new IOPictogram();

                            pictogram.setId(jsonObject.getInt("id"));
                            pictogram.setFilePath(jsonObject.getString("file"));
                            pictogram.setUrl(jsonObject.getString("deepurl"));
                            String text = jsonObject.getString("text");
                            pictogram.setDescription(text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase());
                            //TODO: add type or category

                            mPictograms.add(pictogram);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RemoteImagesGridAdapter gridAdapter = (RemoteImagesGridAdapter) mGridView.getAdapter();
                        gridAdapter.notifyDataSetChanged();
                        gridAdapter.notifyDataSetInvalidated();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                //Timeout, 500, no connection

                Toast.makeText(getApplicationContext(), getString(R.string.request_error), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.smimages_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class RemoteImagesGridAdapter extends ArrayAdapter<IOPictogram> {

        private int resId;

        public RemoteImagesGridAdapter(Context context, int resId) {
            super(context, resId);
            this.resId = resId;
        }

        @Override
        public int getCount() {
            return mPictograms != null ? mPictograms.size() : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = getLayoutInflater().inflate(resId, parent, false);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.ImageSearchCellImageView);

            TextView titleText = (TextView) convertView.findViewById(R.id.ImageSearchCellTitleText);
            TextView categoryText = (TextView) convertView.findViewById(R.id.ImageSearchCellCategoryText);

            IOPictogram pictogram = mPictograms.get(position);

            ImageLoader imageLoader = ImageLoader.getInstance();

            imageLoader.displayImage(pictogram.getUrl(), imageView);

            Log.d("pictogram debug", pictogram.getUrl());

            categoryText.setText(pictogram.getType());
            titleText.setText(pictogram.getDescription());


            return convertView;
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        if (IOHelper.checkForRequiredPermissions(this)) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        if (IOHelper.checkForRequiredPermissions(this)) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
        }
        return false;
    }


}
