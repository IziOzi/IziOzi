package it.smdevelopment.iziozi.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.smdevelopment.iziozi.R;

/**
 * Created by martinolessio on 07/04/14.
 *
 * Main Mappings: (Key) -> (Value Type)
 *
 *      (ROWS_NUM) -> (Integer)
 *      (COLS_NUM) -> (Integer)
 *
 */

@Root(name = "SMIziOziConfiguration")
public class SMIziOziConfiguration {

    Context context;

    @Attribute
    private Integer rows = 2;
    @Attribute
    private Integer cols = 3;

    @ElementList(inline = true, required = false)
    private List<SMSpeakableImageButton> mButtons;

    public SMIziOziConfiguration(){

    }

    public SMIziOziConfiguration(Context ctx){
        this.mButtons = new ArrayList<SMSpeakableImageButton>();
        this.context = ctx;
    }

    public List<SMSpeakableImageButton> getButtons() {
        return mButtons;
    }

    public void setButtons(List<SMSpeakableImageButton> mButtons) {
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

    public void setContext(Context context) {
        this.context = context;
    }

    public void save(){

        Serializer serializer = new Persister();

        File file = new File(this.context.getFilesDir(),"config.xml");

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


    public static SMIziOziConfiguration getSavedConfiguration(){
        Serializer serializer = new Persister();
        File file = new File(SMIziOziApplication.CONTEXT.getFilesDir(),"config.xml");

        SMIziOziConfiguration config = null;

        try {
            config = serializer.read(SMIziOziConfiguration.class, file);
        } catch (Exception e) {
            Log.w("XmlSeializer", "Unable to read config.xml");
            Log.d("XmlSerializer", ""+SMIziOziApplication.CONTEXT.getFilesDir());
            e.printStackTrace();
        }

        return config;

    }


}
