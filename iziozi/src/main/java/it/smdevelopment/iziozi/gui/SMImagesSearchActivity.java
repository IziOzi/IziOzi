package it.smdevelopment.iziozi.gui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

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

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smimages_search);

        mTextView = (TextView) findViewById(R.id.search_text);

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
            mTextView.setText(query);
            searchImages(query);
        }
    }

    private List<Pictogram> searchImages(String queryString) {

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

            ktQueryBuilder.where().like(KeywordText.TEXT_NAME, "%"+queryString+"%");

            SharedPreferences prefs = getSharedPreferences(SMIziOziApplication.APPLICATION_NAME, Context.MODE_PRIVATE);
            int languageId = prefs.getInt(SMIziOziApplication.APPLICATION_LANGUAGE_ID,1);

            lQueryBuilder.where().eq(Language.ID_NAME, languageId);

            PreparedQuery<Pictogram> query = null;

            query = pQueryBuilder.leftJoin(kQueryBuilder.leftJoin(ktQueryBuilder.leftJoin(lQueryBuilder))).prepare();
            mTextView.setText(query.toString());
            //List<Language> languages = languagesDao.query(query);


        } catch (SQLException e) {
            e.printStackTrace();
        }




        return null;
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
}
