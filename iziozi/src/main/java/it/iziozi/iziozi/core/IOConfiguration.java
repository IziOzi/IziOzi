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

package it.iziozi.iziozi.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

/**
 * Created by martinolessio on 05/11/14.
 */

@Root(name = "IOConfiguration")
public class IOConfiguration {

    @Element(required = false)
    private IOLevel mLevel;

    @Attribute(required = false)
    private static Boolean showBorders = true;

    @Attribute(required = false)
    private static Boolean swipeEnabled = true;

    @Attribute(required = false)
    private static Boolean bigNavigation = false;


    public IOConfiguration() {
    }

    public static Boolean isSwipeEnabled() {
        return swipeEnabled;
    }

    public static void setSwipeEnabled(Boolean swipeEnabled) {
        IOConfiguration.swipeEnabled = swipeEnabled;
    }

    public static Boolean isBigNavigation() {
        return bigNavigation;
    }

    public static void setBigNavigation(Boolean bigNavigation) {
        IOConfiguration.bigNavigation = bigNavigation;
    }

    public static Boolean getShowBorders() {
        return showBorders;
    }

    public static void setShowBorders(Boolean showBorders) {
        IOConfiguration.showBorders = showBorders;
    }

    public IOLevel getLevel() {
        if(mLevel == null)
            mLevel = new IOLevel();
        return mLevel;
    }

    public boolean save() {

        return saveAs(null);

    }

    public boolean save(String filename){
        return saveAs(filename);
    }

    private boolean saveAs(String fileName) {

        if(fileName == null)
            fileName = "config";

        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(), fileName + ".xml");

        try {
            serializer.write(this, file);

            SharedPreferences.Editor preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE).edit();
            preferences.putString(IOGlobalConfiguration.IO_LAST_BOARD_USED, fileName + ".xml");

            preferences.commit();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("XmlConfig", "Error writing xml");

            return false;
        }
        return true;
    }


    public static IOConfiguration getSavedConfiguration() {
        return getSavedConfiguration(null);
    }



    public static IOConfiguration getSavedConfiguration(String fileName) {

        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        if(fileName == null){
            SharedPreferences preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);
            preferences.getString(IOGlobalConfiguration.IO_LAST_BOARD_USED, "config.xml");
        }

        File file = new File(dirFile.toString(), fileName);

        IOConfiguration config = null;

        try {
            config = serializer.read(IOConfiguration.class, file);

            SharedPreferences.Editor preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE).edit();
            preferences.putString(IOGlobalConfiguration.IO_LAST_BOARD_USED, fileName);
            preferences.commit();

        } catch (Exception e) {
            Log.w("XmlSeializer", "Unable to read configuration file");
            Log.d("XmlSerializer", "" + IOApplication.CONTEXT.getFilesDir());
            e.printStackTrace();
        }

        return config;

    }

}
