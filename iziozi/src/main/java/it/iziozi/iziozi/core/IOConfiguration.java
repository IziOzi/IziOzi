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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

import it.iziozi.iziozi.R;

/**
 * Created by martinolessio on 05/11/14.
 */

@Root(name = "IOConfiguration")
public class IOConfiguration {

    public static final String IO_LAST_BOARD_USED = "last_board_used";

    private Context context;

    @Element(required = false)
    private IOLevel mLevel;

    @Attribute
    private static Boolean showBorders = true;


    public IOConfiguration() {
    }


    public static Boolean getShowBorders() {
        return showBorders;
    }

    public void setShowBorders(Boolean showBorders) {
        this.showBorders = showBorders;
    }

    public IOLevel getLevel() {
        if(mLevel == null)
            mLevel = new IOLevel();
        return mLevel;
    }

    public void save() {

        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(), "config.xml");

        try {
            serializer.write(this, file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("XmlConfig", "Error writing config.xml");
            new AlertDialog.Builder(context)
                    .setTitle(this.context.getResources().getString(R.string.error))
                    .setMessage(this.context.getResources().getString(R.string.xml_save_fail))
                    .setNegativeButton(this.context.getResources().getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

    }

    public void saveAs(String fileName) {

        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(), fileName + ".xml");

        try {
            serializer.write(this, file);

            SharedPreferences.Editor preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE).edit();
            preferences.putString(IO_LAST_BOARD_USED, fileName + ".xml");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("XmlConfig", "Error writing xml");
            new AlertDialog.Builder(context)
                    .setTitle(this.context.getResources().getString(R.string.error))
                    .setMessage(this.context.getResources().getString(R.string.xml_save_fail))
                    .setNegativeButton(this.context.getResources().getString(R.string.continue_string), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

    }


    public static IOConfiguration getSavedConfiguration() {
        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        SharedPreferences preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE);

        String lastBoard = preferences.getString(IO_LAST_BOARD_USED, "config.xml");

        File file = new File(dirFile.toString(), lastBoard);

        IOConfiguration config = null;

        try {
            config = serializer.read(IOConfiguration.class, file);
        } catch (Exception e) {
            Log.w("XmlSeializer", "Unable to read config.xml");
            Log.d("XmlSerializer", "" + IOApplication.CONTEXT.getFilesDir());
            e.printStackTrace();
        }

        return config;

    }

    public static IOConfiguration getSavedConfiguration(String fileName) {
        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(), fileName);

        IOConfiguration config = null;

        try {
            config = serializer.read(IOConfiguration.class, file);

            SharedPreferences.Editor preferences = IOApplication.CONTEXT.getSharedPreferences(IOApplication.APPLICATION_NAME, Context.MODE_PRIVATE).edit();
            preferences.putString(IO_LAST_BOARD_USED, fileName);


        } catch (Exception e) {
            Log.w("XmlSeializer", "Unable to read config.xml");
            Log.d("XmlSerializer", "" + IOApplication.CONTEXT.getFilesDir());
            e.printStackTrace();
        }

        return config;

    }

}
