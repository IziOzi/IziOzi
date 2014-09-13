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

package it.smdevelopment.iziozi.gui;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
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

import it.smdevelopment.iziozi.R;
import it.smdevelopment.iziozi.core.IOApiClient;
import it.smdevelopment.iziozi.core.IOApplication;
import it.smdevelopment.iziozi.core.IODatabaseHelper;
import it.smdevelopment.iziozi.core.dbclasses.IOPictogram;

public class IORemoteImageSearchActivity extends OrmLiteBaseActivity<IODatabaseHelper> {

    private GridView mGridView;
    private List<IOPictogram> mPictograms;

    ProgressDialog barProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smimages_search);

        mGridView = (GridView) findViewById(R.id.ImageSearchGridView);

        mGridView.setNumColumns(5);
        mGridView.setAdapter(new RemoteImagesGridAdapter(this, R.layout.image_grid_cell));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(isExternalStorageReadable()) {
                    final IOPictogram pictogram = mPictograms.get(position);

                    File baseFolder = new File(Environment.getExternalStorageDirectory() + "/" + IOApplication.APPLICATION_FOLDER + "/pictograms");
                    Character pictoChar = pictogram.getFilePath().charAt(0);
                    File pictoFolder = new File(baseFolder + "/" + pictoChar + "/");
                    final File pictoFile = new File(baseFolder + "/" + pictoChar + "/" + pictogram.getFilePath());

                    final Intent backIntent = new Intent(getApplicationContext(), IOCreateButtonActivity.class);
                    backIntent.putExtra(IOCreateButtonActivity.IMAGE_URL, pictogram.getUrl());

                    if (!pictoFile.exists() && isExternalStorageWritable()) {

                        pictoFolder.mkdirs();

                        //download it

                        barProgressDialog = new ProgressDialog(IORemoteImageSearchActivity.this);
                        barProgressDialog.setTitle("Downloading Image ...");
                        barProgressDialog.setMessage("Download in progress ...");
                        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                        barProgressDialog.setProgress(0);
                        barProgressDialog.setMax(100);
                        barProgressDialog.show();


                        AsyncHttpClient client = new AsyncHttpClient();
                        client.get(pictogram.getUrl(), new FileAsyncHttpResponseHandler(pictoFile) {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                barProgressDialog.cancel();
                                Toast.makeText(getApplicationContext(), "File download error!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onProgress(int bytesWritten, int totalSize) {
                                super.onProgress(bytesWritten, totalSize);

                                int progress = bytesWritten / totalSize;
                                progress *= 100;

                                barProgressDialog.setProgress(progress);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File downloadedFile) {



                                if(pictoFile.exists())
                                {
                                    backIntent.putExtra(IOCreateButtonActivity.IMAGE_FILE, pictoFile.toString());
                                    finish();
                                    startActivity(backIntent);
                                }else
                                {
                                    Toast.makeText(getApplicationContext(), "There was a problem saving the image file!", Toast.LENGTH_SHORT).show();
                                    barProgressDialog.cancel();
                                }
                            }
                        });
                    } else {
                        //file already exists
                        backIntent.putExtra(IOCreateButtonActivity.IMAGE_FILE, pictoFile.toString());
                        finish();
                        startActivity(backIntent);
                    }
                }
            }
        });

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
            searchImages(query);
        }
    }

    private void searchImages(String queryString) {

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

                Toast.makeText(getApplicationContext(), "received an unexpected object!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                //Correct response

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);

                        IOPictogram pictogram = new IOPictogram();

                        pictogram.setId(jsonObject.getInt("id"));
                        pictogram.setFilePath(jsonObject.getString("file"));
                        pictogram.setUrl(jsonObject.getString("deepurl"));
                        pictogram.setDescription(jsonObject.getString("text"));

                        mPictograms.add(pictogram);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RemoteImagesGridAdapter gridAdapter = (RemoteImagesGridAdapter) mGridView.getAdapter();
                    gridAdapter.notifyDataSetChanged();
                    gridAdapter.notifyDataSetInvalidated();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                //Timeout, 500, no connection

                Toast.makeText(getApplicationContext(), "Error on http request!", Toast.LENGTH_SHORT).show();
            }
        });







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

    private class RemoteImagesGridAdapter extends ArrayAdapter<IOPictogram>
    {

        private int resId;

        public RemoteImagesGridAdapter(Context context, int resId)
        {
            super(context,resId);
            this.resId = resId;
        }

        @Override
        public int getCount() {
            return mPictograms != null ? mPictograms.size():0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null)
                convertView = getLayoutInflater().inflate(resId, parent, false);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.ImageSearchCellImageView);

            IOPictogram pictogram = mPictograms.get(position);

            ImageLoader imageLoader = ImageLoader.getInstance();

            imageLoader.displayImage(pictogram.getUrl(), imageView);

            Log.d("pictogram debug", pictogram.getUrl());


            return convertView;
        }
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