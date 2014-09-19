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

package it.iziozi.iziozi.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IODatabaseHelper;


public class IOCreateButtonActivity extends OrmLiteBaseActivity<IODatabaseHelper> {

    public final static String IMAGE_FILE = "image_file";
    public final static String IMAGE_TITLE = "image_title";
    public final static String IMAGE_URL = "image_url";

    private final static int IMAGE_CAMERA_PICK_INTENT = 101;
    private final static int IMAGE_GALLERY_PICK_INTENT = 100;

    private SearchView mSearchView;
    private ImageButton mImageButton;
    private EditText mTitleText, mTextText;
    private TextView mTapHereTextView;

    private String mImageFile;
    private String mImageTitle;
    private String mImageText;
    private String mImageUrl;

    private File mFileDir, mDestinationFile, mFile;
    private String cameraFile = null;

    private int mButtonIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mImageTitle = extras.getString(IOBoardActivity.BUTTON_TITLE);
            mImageFile = extras.getString(IOBoardActivity.BUTTON_IMAGE_FILE);
            mImageText = extras.getString(IOBoardActivity.BUTTON_TEXT);
            mImageUrl = extras.getString(IOBoardActivity.BUTTON_URL);

        }

        mButtonIndex = getIntent().getExtras().getInt(IOBoardActivity.BUTTON_INDEX);

        setContentView(R.layout.create_button_activity_layout);

        mImageButton = (ImageButton) findViewById(R.id.CreateButtonImageBtn);
        mTitleText = (EditText) findViewById(R.id.CreateButtonTitleText);
        mTextText = (EditText) findViewById(R.id.CreateButtonTextText);
        mTapHereTextView = (TextView) findViewById(R.id.CreateButtonTapLabel);

        if (mImageTitle != null)
            mTitleText.setText(mImageTitle);

        if (mImageText != null)
            mTextText.setText(mImageText);

        if (mImageFile != null && mImageFile.length() > 0) {
            mImageButton.setImageBitmap(BitmapFactory.decodeFile(mImageFile));
            mTapHereTextView.setVisibility(View.INVISIBLE);
        }


        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.data_connection_needed))
                    .setNegativeButton(getString(R.string.continue_string), null)
                    .create()
                    .show();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        String pictoFile = extras.getString(IMAGE_FILE);

        String pictoUrl = extras.getString(IMAGE_URL);
        String pictoTitle = extras.getString(IMAGE_TITLE);

        if (pictoFile != null) {
            mImageFile = pictoFile;

            mImageButton.setImageBitmap(BitmapFactory.decodeFile(pictoFile));
            mTapHereTextView.setVisibility(View.INVISIBLE);
        }

        if (pictoUrl != null) {
            mImageUrl = pictoUrl;
        }

        mImageTitle = pictoTitle;

        mTitleText.setText(pictoTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();

/*
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_button, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);


        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    mSearchView.setIconified(true);
            }
        });

        mSearchView = searchView;

        return true;
    }

    public void doSave(View v) {
        Intent resultIntent = new Intent();
        if (mImageFile != null)
            resultIntent.putExtra(IOBoardActivity.BUTTON_IMAGE_FILE, mImageFile);

        mImageTitle = mTitleText.getText().toString();

        if (mImageTitle != null && mImageTitle.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_TITLE, mImageTitle);

        mImageText = mTextText.getText().toString();

        if (mImageText != null && mImageText.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_TEXT, mImageText);

        if (mImageUrl != null && mImageUrl.length() > 0)
            resultIntent.putExtra(IOBoardActivity.BUTTON_URL, mImageUrl);

        resultIntent.putExtra(IOBoardActivity.BUTTON_INDEX, mButtonIndex);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void doTapOnImage(View v) {


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        adapter.add(getResources().getString(R.string.img_search));
        adapter.add(getResources().getString(R.string.img_gallery));
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            adapter.add(getResources().getString(R.string.img_camera));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choose))
                .setAdapter(adapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("media_debug", "click on item " + which);
                        if (which == 0)
                            searchImage();
                        else if (which == 1)
                            pickFromGallery();
                        else if(which == 2)
                            pickFromCamera();

                    }
                }).setNegativeButton(getResources().getString(R.string.cancel), null)
                .create().show();


    }

    private void searchImage()
    {
        mSearchView.setIconified(false);
        mSearchView.requestFocus();

    }

    private void pickFromCamera() {

        mFileDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/camera");
        if (!mFileDir.isDirectory())
            mFileDir.mkdirs();

        mDestinationFile = new File(mFileDir, new Date().getTime() + ".jpg");
        cameraFile = mDestinationFile.getAbsolutePath();
        try{
            if(!mDestinationFile.createNewFile())
                Log.e("check", "unable to create empty file");

            mFile = new File(mDestinationFile.getAbsolutePath());
            Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mDestinationFile));
            startActivityForResult(i,IMAGE_CAMERA_PICK_INTENT);

        }catch(IOException ex){
            ex.printStackTrace();
        }


    }

    private void pickFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, IMAGE_GALLERY_PICK_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        Log.d("img_picker_debug", "enter with code "+resultCode +"req:"+requestCode);

        if(resultCode == Activity.RESULT_OK)
        {
            switch (requestCode) {
                case IMAGE_GALLERY_PICK_INTENT:
                {

                    Uri selectedImage = data.getData();
                    Log.d("image_debug", selectedImage.toString());
                    InputStream imageStream;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        Log.d("image_debug", "onActivityResult:"+bitmap.getWidth()+" " + bitmap.getHeight());
                        if(bitmap.getHeight()>=2048||bitmap.getWidth()>=2048){
                            bitmap = scaleToFill(bitmap, 1024, 1024);
                        }

                        mFileDir = new File(Environment.getExternalStorageDirectory()
                                .getAbsolutePath(), IOApplication.APPLICATION_NAME + "/gallery");
                        if (!mFileDir.isDirectory())
                            mFileDir.mkdirs();

                        mDestinationFile = new File(mFileDir, new Date().getTime() + ".jpg");

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(mDestinationFile);

                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        mImageButton.setImageBitmap(bitmap);
                        mImageFile = mDestinationFile.toString();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case IMAGE_CAMERA_PICK_INTENT:
                {
                    if(mFile ==null){
                        if(cameraFile!=null)
                            mFile = new File(cameraFile);
                        else
                            Log.e("check", "camera file object null");
                    }else
                        Log.e("check", mFile.getAbsolutePath());
                    Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
                    if(bitmap.getHeight()>=2048||bitmap.getWidth()>=2048){
                        bitmap = scaleToFill(bitmap, 1024, 1024);
                    }

                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(cameraFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    mImageButton.setImageBitmap(bitmap);
                    mImageFile = mFile.toString();

                    break;
                }

                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    public Bitmap scaleToFill(Bitmap b, int width, int height) {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);
    }


}
