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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "IOSpeakableImageButton")
public class IOSpeakableImageButton extends ImageButton {

    @Element(required = false)
    private String mSentence = "";

    @Element(required = false)
    private String mImageFile = "";

    @Element(required = false)
    private String mAudioFile = "";

    @Element(required = false)
    private String mVideoFile = "";

    @Element(required = false)
    private String mTitle = "";

    @Element(required = false)
    private String mUrl = "";

    @Element(required = false)
    private String mIntentPackageName = "";

    @Element(required = false)
    private String mIntentName = "";

    /*
    * Nested boards
    * */
    @Attribute
    private Boolean mIsMatrioska = false;

    @Element(required = false)
    private IOLevel mLevel;

    private Boolean mIsHiglighted = false;

    private Context mContext;

    private boolean mShowBorder;

    private int mPaddingWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
    ;


    public IOSpeakableImageButton(Context ctx) {
        super(ctx);
        mContext = ctx;

        setPadding(mPaddingWidth, mPaddingWidth, mPaddingWidth, mPaddingWidth);

        setScaleType(ScaleType.CENTER_INSIDE);

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

    public IOSpeakableImageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setPadding(mPaddingWidth, mPaddingWidth, mPaddingWidth, mPaddingWidth);
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    public IOSpeakableImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPadding(mPaddingWidth, mPaddingWidth, mPaddingWidth, mPaddingWidth);
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    public String getIntentPackageName() {
        return mIntentPackageName;
    }

    public void setIntentPackageName(String mIntentPackageName) {
        this.mIntentPackageName = mIntentPackageName;
    }

    public String getIntentName() {
        return mIntentName;
    }

    public void setIntentName(String mIntentName) {
        this.mIntentName = mIntentName;
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

/*
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT )
            return mImageFile.replace("emulated/0", "sdcard0");
*/
        if(mImageFile == null)
            return mImageFile;

        if(mImageFile.indexOf("IziOzi") != -1)
            return Environment.getExternalStorageDirectory() + "/iziozi" + mImageFile.split("IziOzi")[1];

        if(mImageFile.indexOf("iziozi") != -1)
            return Environment.getExternalStorageDirectory() + "/iziozi" + mImageFile.split("iziozi")[1];


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

    public String getVideoFile() {
        return mVideoFile;
    }

    public void setVideoFile(String mVideoFile) {
        this.mVideoFile = mVideoFile;
    }

    public Boolean getIsMatrioska() {
        return mIsMatrioska;
    }

    public void setIsMatrioska(Boolean mIsMatrioska) {
        this.mIsMatrioska = mIsMatrioska;
    }

    public IOLevel getLevel() {

        if(null == mLevel)
            mLevel = new IOLevel();

        if(mLevel.getBoardAtIndex(0) == null)
        {
            mLevel.addInnerBoard(new IOBoard());
        }

        return mLevel;
    }


    public Boolean getIsHiglighted() {
        return mIsHiglighted;
    }

    public void setIsHiglighted(Boolean mIsHiglighted) {
        this.mIsHiglighted = mIsHiglighted;
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

            if(null == d)
                return;

            final int origW = d.getIntrinsicWidth();
            final int origH = d.getIntrinsicHeight();

            final int actW = Math.round(origW * scaleX);
            final int actH = Math.round(origH * scaleY);

            int borderWidth = mIsHiglighted ? 6 : 4;

            Paint paint = new Paint();
            paint.setColor(mIsHiglighted ? Color.RED : Color.BLACK);
            paint.setStrokeWidth((mIsHiglighted ? 3:2) * borderWidth);
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
