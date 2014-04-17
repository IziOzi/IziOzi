package it.smdevelopment.iziozi.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by martinolessio on 07/04/14.
 */
public class SMIziOziApplication extends Application {

    public static Context CONTEXT;
    public static final String APPLICATION_NAME = "iziozi";
    private SMIziOziDatabaseHelper openedDb;

    private SQLiteDatabase connectedDb = null;


    @Override
    public void onCreate() {
        super.onCreate();

        SMIziOziApplication.CONTEXT = getApplicationContext();

        SharedPreferences prefs = getSharedPreferences(APPLICATION_NAME, Context.MODE_PRIVATE);
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

        prefsEditor.commit();

    }
}
