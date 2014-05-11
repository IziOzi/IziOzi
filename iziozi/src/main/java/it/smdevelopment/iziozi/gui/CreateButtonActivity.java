package it.smdevelopment.iziozi.gui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import it.smdevelopment.iziozi.R;
import it.smdevelopment.iziozi.core.SMIziOziDatabaseHelper;

public class CreateButtonActivity extends OrmLiteBaseActivity<SMIziOziDatabaseHelper> {

    private SearchView mSearchView;
    private ImageButton mImageButton;
    private EditText mTitleText, mTextText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.create_button_activity_layout);

        mImageButton = (ImageButton) findViewById(R.id.CreateButtonImageBtn);
        mTitleText = (EditText) findViewById(R.id.CreateButtonTitleText);
        mTextText = (EditText) findViewById(R.id.CreateButtonTextText);

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
        Toast.makeText(getApplicationContext(),"save!",Toast.LENGTH_SHORT).show();
    }

    public void doTapOnImage(View v){
        mSearchView.setIconified(false);
        mSearchView.requestFocus();
    }


}
