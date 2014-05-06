package it.smdevelopment.iziozi.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import it.smdevelopment.iziozi.core.dbclasses.Language;

/**
 * Created by martinolessio on 07/04/14.
 */
public class SMIziOziApplication extends Application {

    public static Context CONTEXT;
    public static String applicationLocale;
    public static final String APPLICATION_NAME = "iziozi";
    public static final String APPLICATION_LOCALE = "APPLICATION_LOCALE";
    private SMIziOziDatabaseHelper openedDb;

    private SQLiteDatabase connectedDb = null;


    @Override
    public void onCreate() {
        super.onCreate();

        SMIziOziApplication.CONTEXT = getApplicationContext();

        SharedPreferences prefs = getSharedPreferences(APPLICATION_NAME, Context.MODE_PRIVATE);

        applicationLocale = prefs.getString(APPLICATION_LOCALE, Locale.getDefault().getLanguage());

        SharedPreferences.Editor prefsEditor = prefs.edit();


        int sqliteVersionUpdate = 20131118;
        int sqliteVersionActual = prefs.getInt("sqlite_version", 0);

        if(sqliteVersionActual < sqliteVersionUpdate )
        {
            File dbFile = new File(SMIziOziDatabaseHelper.getDbFullPath());
            if(dbFile.exists())
                dbFile.delete();
        }

        this.openedDb = new SMIziOziDatabaseHelper(getApplicationContext());
        this.openedDb.createDataBase();
        this.openedDb.openDataBase();
        connectedDb = openedDb.getSharedDb();
        prefsEditor.putInt("sqlite_version", sqliteVersionUpdate);



        OpenHelperManager.setHelper(openedDb);

        try{


            Dao<Language,String> languagesDao = openedDb.getDao(Language.class);



            QueryBuilder<Language, String> lQueryBuilder = languagesDao.queryBuilder();

            lQueryBuilder.where().eq(Language.CODE_NAME, applicationLocale);

            PreparedQuery<Language> query = lQueryBuilder.prepare();

            Log.d("query debug", query.toString());

            List<Language> languages = languagesDao.query(query);

            if(languages.size() > 0)
            {
                Log.d("locale debug", languages.get(0).getName());
                prefsEditor.putString(APPLICATION_LOCALE, languages.get(0).getCode());

            }else
            {
                Log.d("locale debug", "using default locale. requested: " + applicationLocale);
                prefsEditor.putString(APPLICATION_LOCALE, Locale.ENGLISH.getLanguage());
            }
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        prefsEditor.commit();

        Log.d("database debug", "database is: " + this.connectedDb);

    }
}
