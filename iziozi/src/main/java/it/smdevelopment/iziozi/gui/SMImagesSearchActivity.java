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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import it.smdevelopment.iziozi.R;
import it.smdevelopment.iziozi.core.SMIziOziApplication;
import it.smdevelopment.iziozi.core.SMIziOziDatabaseHelper;
import it.smdevelopment.iziozi.core.dbclasses.Keyword;
import it.smdevelopment.iziozi.core.dbclasses.KeywordText;
import it.smdevelopment.iziozi.core.dbclasses.Language;
import it.smdevelopment.iziozi.core.dbclasses.Pictogram;

public class SMImagesSearchActivity extends OrmLiteBaseActivity<SMIziOziDatabaseHelper> {

    private GridView mGridView;
    private List<Pictogram> mPictograms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smimages_search);

        mGridView = (GridView) findViewById(R.id.ImageSearchGridView);

        mGridView.setNumColumns(5);
        mGridView.setAdapter(new ImagesGridAdapter(this, R.layout.image_grid_cell));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pictogram pictogram = mPictograms.get(position);

                Intent backIntent = new Intent(getApplicationContext(), CreateButtonActivity.class);

                File pictoFile =new File( Environment.getExternalStorageDirectory() + "/" + SMIziOziApplication.APPLICATION_FOLDER + "/pictograms" );
                Character pictoFolder = pictogram.getFilePath().charAt(0);

                pictoFile = new File(pictoFile + "/" + pictoFolder + "/" + pictogram.getFilePath());
                backIntent.putExtra(CreateButtonActivity.IMAGE_FILE, pictoFile.toString() );

                finish();
                startActivity(backIntent);
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

        String[] queryList = queryString.split("\\s+");

        try {
            Dao<Pictogram, String> pictoDao = getHelper().getDao(Pictogram.class);
            Dao<Keyword, String> keywordDao = getHelper().getDao(Keyword.class);
            Dao<KeywordText, String> keywordTextDao = getHelper().getDao(KeywordText.class);
            Dao<Language, String> languagedDao = getHelper().getDao(Language.class);

            QueryBuilder<Pictogram, String> pQueryBuilder = pictoDao.queryBuilder();
            QueryBuilder<Keyword, String> kQueryBuilder = keywordDao.queryBuilder();
            QueryBuilder<KeywordText, String> ktQueryBuilder = keywordTextDao.queryBuilder();
            QueryBuilder<Language, String> lQueryBuilder = languagedDao.queryBuilder();

            pQueryBuilder.distinct().orderBy(Pictogram.FILE_NAME, true);

            ktQueryBuilder.where().like(KeywordText.TEXT_NAME, "%"+queryString+"%");

            SharedPreferences prefs = getSharedPreferences(SMIziOziApplication.APPLICATION_NAME, Context.MODE_PRIVATE);
            int languageId = prefs.getInt(SMIziOziApplication.APPLICATION_LANGUAGE_ID,1);

            lQueryBuilder.where().eq(Language.ID_NAME, languageId);

            PreparedQuery<Pictogram> query = null;

            query = pQueryBuilder.leftJoin(kQueryBuilder.leftJoin(ktQueryBuilder.leftJoin(lQueryBuilder))).prepare();
            mPictograms = pictoDao.query(query);


        } catch (SQLException e) {
            e.printStackTrace();
        }

        ImagesGridAdapter gridAdapter = (ImagesGridAdapter) mGridView.getAdapter();
        gridAdapter.notifyDataSetChanged();
        gridAdapter.notifyDataSetInvalidated();

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

    private class ImagesGridAdapter extends ArrayAdapter<Pictogram>
    {

        private int resId;
        private File filesRoot;

        public ImagesGridAdapter(Context context, int resId)
        {
            super(context,resId);
            this.resId = resId;
            this.filesRoot = new File(Environment.getExternalStorageDirectory() + "/" + SMIziOziApplication.APPLICATION_FOLDER + "/pictograms");
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

            Pictogram pictogram = mPictograms.get(position);
            Character pictoFolder = pictogram.getFilePath().charAt(0);

            File pictoFile = new File(filesRoot + "/" + pictoFolder + "/" + pictogram.getFilePath());

            Log.d("pictogram debug", pictoFile.toString());

            imageView.setImageBitmap(BitmapFactory.decodeFile(pictoFile.toString()));

            return convertView;
        }
    }
}
