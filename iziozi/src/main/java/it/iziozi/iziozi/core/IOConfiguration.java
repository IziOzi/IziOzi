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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Created by martinolessio on 05/11/14.
 */

@Root(name = "IOConfiguration")
public class IOConfiguration {

    @Element(required = false)
    @SerializedName("board")
    @Expose
    private IOLevel mLevel;

    @Attribute(required = false)
    @SerializedName("showBorders")
    @Expose
    private static Boolean showBorders = true;

    @Attribute(required = false)
    @SerializedName("swipeEnabled")
    @Expose
    private static Boolean swipeEnabled = true;

    @Attribute(required = false)
    @SerializedName("bigNavigation")
    @Expose
    private static Boolean bigNavigation = false;

    @Attribute(required = false)
    @SerializedName("showLabels")
    @Expose
    private static Boolean showLabels = false;


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

    public static Boolean isShowLabels() {
        return showLabels;
    }

    public static void setShowLabels(Boolean showLabels) {
        IOConfiguration.showLabels = showLabels;
    }

    public IOLevel getLevel() {
        if (mLevel == null)
            mLevel = new IOLevel();
        return mLevel;
    }

    public boolean save() {

        return saveAs(null);

    }

    public boolean save(String filename) {
        return saveAs(filename);
    }

    private boolean saveAs(String fileName) {

        if (fileName == null)
            fileName = "config";

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        final GsonBuilder builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting();

        final Gson gson = builder.create();
        String jsonString = gson.toJson(this);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(dirFile.toString(), fileName + ".json")), Charset.forName("UTF-8").newEncoder());
            outputStreamWriter.write(jsonString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        return true;
    }


    public static IOConfiguration getSavedConfiguration() {
        return getSavedConfiguration(null);
    }


    public static IOConfiguration getSavedConfiguration(String fileName) {

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        if (fileName == null) {
            SharedPreferences preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);
            fileName = preferences.getString(IOGlobalConfiguration.IO_LAST_BOARD_USED, "config.xml");
        }

        File file = new File(dirFile.toString(), fileName);
        File jsonFile = new File(dirFile.toString(), fileName.replace("xml", "json"));

        IOConfiguration config = null;

        String jsonString = "";

        if(jsonFile.exists()){
            //load from json

            Log.d("DEBUG", "trying to load JSON file: " + jsonFile);

            FileInputStream fin = null;
            try {
                fin = new FileInputStream(jsonFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                jsonString = sb.toString();
                //Make sure you close all streams.
                fin.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("DEBUG", "loaded JSON string: " + jsonString.length());


            final GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();

            try{
                config = gson.fromJson(jsonString, IOConfiguration.class);

                SharedPreferences.Editor preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE).edit();
                preferences.putString(IOGlobalConfiguration.IO_LAST_BOARD_USED, fileName);
                preferences.commit();

                Log.d("DEBUG", "loaded configuration from JSON");
            }catch (JsonSyntaxException e){
                e.printStackTrace();
                Log.w("DEBUG", "error loading configuration from JSON");
            }


        }else{
            Serializer serializer = new Persister();

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

            Log.d("DEBUG", "loaded configuration from XML");
        }


        return config;

    }

}
