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
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import it.smdevelopment.iziozi.R;
import it.smdevelopment.iziozi.core.dbclasses.IOLanguage;

/**
 * Created by martinolessio on 07/04/14.
 */
public class IOApplication extends Application {

    public static Context CONTEXT;
    public static String applicationLocale;
    public static final String APPLICATION_NAME = "iziozi";
    public static final String APPLICATION_LOCALE = "APPLICATION_LOCALE";
    public static final String APPLICATION_LANGUAGE_ID = "APPLICATION_LANGUAGE_ID";
    public static final String APPLICATION_FOLDER = "IziOzi";

    private IODatabaseHelper openedDb;

    private SQLiteDatabase connectedDb = null;


    @Override
    public void onCreate() {
        super.onCreate();

        IOApplication.CONTEXT = getApplicationContext();

        SharedPreferences prefs = getSharedPreferences(APPLICATION_NAME, Context.MODE_PRIVATE);

        applicationLocale = prefs.getString(APPLICATION_LOCALE, Locale.getDefault().getLanguage());

        SharedPreferences.Editor prefsEditor = prefs.edit();


        int sqliteVersionUpdate = 20131118;
        int sqliteVersionActual = prefs.getInt("sqlite_version", 0);

        if (sqliteVersionActual < sqliteVersionUpdate) {
            File dbFile = new File(IODatabaseHelper.getDbFullPath());
            if (dbFile.exists())
                dbFile.delete();
        }

        this.openedDb = new IODatabaseHelper(getApplicationContext());
        this.openedDb.createDataBase();
        this.openedDb.openDataBase();
        connectedDb = openedDb.getSharedDb();
        prefsEditor.putInt("sqlite_version", sqliteVersionUpdate);


        OpenHelperManager.setHelper(openedDb);

        try {

            Dao<IOLanguage, String> languagesDao = openedDb.getDao(IOLanguage.class);

            QueryBuilder<IOLanguage, String> lQueryBuilder = languagesDao.queryBuilder();

            lQueryBuilder.where().eq(IOLanguage.CODE_NAME, applicationLocale);

            PreparedQuery<IOLanguage> query = lQueryBuilder.prepare();

            Log.d("query debug", query.toString());

            List<IOLanguage> languages = languagesDao.query(query);

            if (languages.size() > 0) {
                Log.d("locale debug", languages.get(0).getName());
                prefsEditor.putString(APPLICATION_LOCALE, languages.get(0).getCode());
                prefsEditor.putInt(APPLICATION_LANGUAGE_ID, languages.get(0).getId());

            } else {
                lQueryBuilder.reset();

                lQueryBuilder.where().eq(IOLanguage.CODE_NAME, Locale.ENGLISH.getLanguage());

                query = lQueryBuilder.prepare();

                languages = languagesDao.query(query);

                Log.d("locale debug", "using default locale. requested: " + applicationLocale);
                prefsEditor.putString(APPLICATION_LOCALE, languages.get(0).getCode());
                prefsEditor.putInt(APPLICATION_LANGUAGE_ID, languages.get(0).getId());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        prefsEditor.commit();

        IOApiClient.setupClient();

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                                .showImageOnLoading(getResources().getDrawable(R.drawable.logo_org))
                                .cacheOnDisk(true)
                                .build()
                )
                .writeDebugLogs()
                .build();

        ImageLoader.getInstance().init(config);


        Log.d("database debug", "database is: " + this.connectedDb);

    }
}
