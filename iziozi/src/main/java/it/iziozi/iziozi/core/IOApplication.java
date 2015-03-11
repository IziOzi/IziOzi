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

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import io.fabric.sdk.android.Fabric;
import it.iziozi.iziozi.R;

/**
 * Created by martinolessio on 07/04/14.
 */
public class IOApplication extends Application {

    public static Context CONTEXT;
    public static String applicationLocale;
    public static final String APPLICATION_NAME = "iziozi";
    public static final String APPLICATION_LOCALE = "APPLICATION_LOCALE";
    public static final String APPLICATION_LANGUAGE_ID = "APPLICATION_LANGUAGE_ID";
    public static final String APPLICATION_FOLDER = "IziOzi";


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        IOApplication.CONTEXT = getApplicationContext();

        IOApiClient.setupClient();


        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                                .showImageOnLoading(getResources().getDrawable(R.drawable.logo_org))
                                .cacheOnDisk(true)
                                .cacheInMemory(true)
                                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                                .bitmapConfig(Bitmap.Config.ALPHA_8) // default
                                .build()
                )
                .writeDebugLogs()
                .build();

        ImageLoader.getInstance().init(config);

    }
}
