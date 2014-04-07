package it.smdevelopment.iziozi.core;

import android.app.Application;
import android.content.Context;

/**
 * Created by martinolessio on 07/04/14.
 */
public class SMIziOziApplication extends Application {

    public static Context CONTEXT;


    @Override
    public void onCreate() {
        super.onCreate();

        SMIziOziApplication.CONTEXT = getApplicationContext();

    }
}
