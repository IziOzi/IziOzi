/*
 * Copyright (c) 2015 Martino Lessio -
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

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by martinolessio on 11/04/15.
 *
 * Inspirated by http://stackoverflow.com/a/15329255/1826349
 *
 */
public class IOInfoObject {
    public String appname = "";
    public String pname = "";
    public String versionName = "";
    public int versionCode = 0;
    public Drawable icon;


    public void InfoObjectAggregatePrint() {//not used yet
        Log.v(appname, appname + "\t" + pname + "\t" + versionName + "\t" + versionCode);
    }

}
