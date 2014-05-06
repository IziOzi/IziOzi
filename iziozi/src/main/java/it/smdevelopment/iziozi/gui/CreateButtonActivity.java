package it.smdevelopment.iziozi.gui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import it.smdevelopment.iziozi.R;
import it.smdevelopment.iziozi.core.SMIziOziApplication;
import it.smdevelopment.iziozi.core.SMIziOziDatabaseHelper;
import it.smdevelopment.iziozi.core.dbclasses.Language;

public class CreateButtonActivity extends OrmLiteBaseActivity<SMIziOziDatabaseHelper> {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.create_button_activity_layout);




	}

    public void doSave(View v){
        Toast.makeText(getApplicationContext(),"save!",Toast.LENGTH_SHORT).show();
    }


}
