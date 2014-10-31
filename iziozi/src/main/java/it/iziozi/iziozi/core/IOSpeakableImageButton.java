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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import it.iziozi.iziozi.R;

@Root(name = "SMSpeakableImageButton")
public class IOSpeakableImageButton extends ImageButton {

    @Element(required = false)
    private String mSentence = "";

    @Element(required = false)
    private String mImageFile = "";

    @Element(required = false)
    private String mAudioFile = "";

    @Element(required = false)
    private String mTitle = "";

    @Element(required = false)
    private String mUrl = "";

    private Context mContext;

    private Boolean mShowBorder;

    private int mPaddingWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
    ;


    public IOSpeakableImageButton(Context ctx) {
        super(ctx);
        mContext = ctx;

        setPadding(mPaddingWidth, mPaddingWidth, mPaddingWidth, mPaddingWidth);

    }

    public IOSpeakableImageButton(@Element(name = "mSentence") String sentence) {
        super(IOApplication.CONTEXT);
        this.mSentence = sentence;

        setPadding(mPaddingWidth, mPaddingWidth, mPaddingWidth, mPaddingWidth);
    }

    public IOSpeakableImageButton() {
        super(IOApplication.CONTEXT);

        setPadding(mPaddingWidth, mPaddingWidth, mPaddingWidth, mPaddingWidth);
    }

    public String getmSentence() {
        return mSentence;
    }

    public void setmSentence(String mSentence) {
        this.mSentence = mSentence;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setSentence(String sentence) {
        mSentence = sentence;
    }

    public String getSentence() {
        return mSentence;
    }

    public String getmImageFile() {
        return mImageFile;
    }

    public void setmImageFile(String mImageFile) {
        this.mImageFile = mImageFile;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setShowBorder(Boolean show) {
        mShowBorder = show;
    }

    public String getAudioFile() {
        return mAudioFile;
    }

    public void setAudioFile(String mAudioFile) {
        this.mAudioFile = mAudioFile;
    }

    public void showInsertDialog() {
        Log.d("home debug", "options selected");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layoutView = inflater.inflate(R.layout.spkbtn_input_layout, null);

        final EditText txt = (EditText) layoutView.findViewById(R.id.textField);
        txt.setText(this.mSentence);

        builder.setTitle("Text to speech")
                .setView(layoutView)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("dialog", "should dismiss and apply ");

                        setSentence(txt.getText().toString());
                    }
                })
                .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("dialog", "should dismiss and discard");
                    }
                });


        builder.create().show();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowBorder) {

            float[] f = new float[9];
            getImageMatrix().getValues(f);

            final float scaleX = f[Matrix.MSCALE_X];
            final float scaleY = f[Matrix.MSCALE_Y];

            final Drawable d = getDrawable();
            final int origW = d.getIntrinsicWidth();
            final int origH = d.getIntrinsicHeight();

            final int actW = Math.round(origW * scaleX);
            final int actH = Math.round(origH * scaleY);

            int borderWidth = 4;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2 * borderWidth);
            paint.setStyle(Paint.Style.STROKE);


            Rect r = new Rect();

            r.top = Math.max(mPaddingWidth / 2, getHeight() / 2 - actH / 2 - mPaddingWidth);
            r.left = Math.max(mPaddingWidth / 2, getWidth() / 2 - actW / 2 - mPaddingWidth);
            r.right = Math.min(getWidth() - mPaddingWidth / 2, getWidth() / 2 + actW / 2 + mPaddingWidth);
            r.bottom = Math.min(getHeight() - mPaddingWidth / 2, getHeight() / 2 + actH / 2 + mPaddingWidth);

            canvas.drawRect(r, paint);
        }

    }
}
