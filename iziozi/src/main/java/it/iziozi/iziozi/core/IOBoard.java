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
import android.os.Environment;
import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.iziozi.iziozi.R;

/**
 * Created by martinolessio on 07/04/14.
 *
 * Main Mappings: (Key) -> (Value Type)
 *
 *      (ROWS_NUM) -> (Integer)
 *      (COLS_NUM) -> (Integer)
 *
 */

@Root(name = "IOBoard")
public class IOBoard {

    Context context;

    @Attribute
    private Integer rows = 2;
    @Attribute
    private Integer cols = 3;

    @Attribute
    private Boolean showBorders = true;

    @ElementList(inline = true, required = false)
    private List<IOSpeakableImageButton> mButtons;

    public IOBoard(){

    }

    public IOBoard(Context ctx){
        this.mButtons = new ArrayList<IOSpeakableImageButton>();
        this.context = ctx;
    }

    public List<IOSpeakableImageButton> getButtons() {
        return mButtons;
    }

    public void setButtons(List<IOSpeakableImageButton> mButtons) {
        this.mButtons = mButtons;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer mRows) {
        this.rows = mRows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols(Integer mCols) {
        this.cols = mCols;
    }

    public Boolean getShowBorders() {
        return showBorders;
    }

    public void setShowBorders(Boolean showBorders) {
        this.showBorders = showBorders;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void save(){

        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(),"config.xml");

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

    public void saveAs(String fileName){

        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(),fileName + ".xml");

        try {
            serializer.write(this, file);
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


    public static IOBoard getSavedConfiguration(){
        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(),"config.xml");

        IOBoard config = null;

        try {
            config = serializer.read(IOBoard.class, file);
        } catch (Exception e) {
            Log.w("XmlSeializer", "Unable to read config.xml");
            Log.d("XmlSerializer", ""+ IOApplication.CONTEXT.getFilesDir());
            e.printStackTrace();
        }

        return config;

    }

    public static IOBoard getSavedConfiguration(String fileName){
        Serializer serializer = new Persister();

        File dirFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/boards");
        if (!dirFile.exists())
            dirFile.mkdirs();

        File file = new File(dirFile.toString(), fileName);

        IOBoard config = null;

        try {
            config = serializer.read(IOBoard.class, file);
        } catch (Exception e) {
            Log.w("XmlSeializer", "Unable to read config.xml");
            Log.d("XmlSerializer", ""+ IOApplication.CONTEXT.getFilesDir());
            e.printStackTrace();
        }

        return config;

    }



}
