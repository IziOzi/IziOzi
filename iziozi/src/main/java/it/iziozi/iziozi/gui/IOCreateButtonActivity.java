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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IODatabaseHelper;


public class IOCreateButtonActivity extends OrmLiteBaseActivity<IODatabaseHelper> {

    public final static String IMAGE_FILE = "image_file";
    public final static String IMAGE_TITLE = "image_title";
    public final static String IMAGE_URL = "image_url";

    private SearchView mSearchView;
    private ImageButton mImageButton;
    private EditText mTitleText, mTextText;
    private TextView mTapHereTextView;

    private String mImageFile;
    private String mImageTitle;
    private String mImageText;
    private String mImageUrl;

    private int mButtonIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            mImageTitle = extras.getString(IOBoardActivity.BUTTON_TITLE);
            mImageFile = extras.getString(IOBoardActivity.BUTTON_IMAGE_FILE);
            mImageText = extras.getString(IOBoardActivity.BUTTON_TEXT);
            mImageUrl = extras.getString(IOBoardActivity.BUTTON_URL);

        }

        mButtonIndex = getIntent().getExtras().getInt(IOBoardActivity.BUTTON_INDEX);
		
		setContentView(R.layout.create_button_activity_layout);

        mImageButton = (ImageButton) findViewById(R.id.CreateButtonImageBtn);
        mTitleText = (EditText) findViewById(R.id.CreateButtonTitleText);
        mTextText = (EditText) findViewById(R.id.CreateButtonTextText);
        mTapHereTextView = (TextView) findViewById(R.id.CreateButtonTapLabel);

        if(mImageTitle != null)
            mTitleText.setText(mImageTitle);

        if(mImageText != null)
            mTextText.setText(mImageText);

        if(mImageFile != null && mImageFile.length() > 0) {
            mImageButton.setImageBitmap(BitmapFactory.decodeFile(mImageFile));
            mTapHereTextView.setVisibility(View.INVISIBLE);
        }


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

        if(pictoFile != null)
        {
            mImageFile = pictoFile;

            mImageButton.setImageBitmap(BitmapFactory.decodeFile(pictoFile));
            mTapHereTextView.setVisibility(View.INVISIBLE);
        }

        if(pictoUrl != null)
        {
            mImageUrl = pictoUrl;
        }

        mImageTitle = pictoTitle;

        mTitleText.setText(pictoTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_button, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);


        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(! hasFocus)
                    mSearchView.setIconified(true);
            }
        });

        mSearchView = searchView;

        return true;
    }

    public void doSave(View v){
        Intent resultIntent = new Intent();
        if(mImageFile != null)
            resultIntent.putExtra(IOBoardActivity.BUTTON_IMAGE_FILE, mImageFile);

        mImageTitle = mTitleText.getText().toString();

        if(mImageTitle != null && mImageTitle.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_TITLE, mImageTitle);

        mImageText = mTextText.getText().toString();

        if(mImageText != null && mImageText.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_TEXT, mImageText);

        if(mImageUrl != null && mImageUrl.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_URL, mImageUrl);

        resultIntent.putExtra(IOBoardActivity.BUTTON_INDEX, mButtonIndex);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void doTapOnImage(View v){
        mSearchView.setIconified(false);
        mSearchView.requestFocus();
    }


}
