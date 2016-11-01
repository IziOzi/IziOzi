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

package it.iziozi.iziozi.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IOInfoObject;

/**
 * Created by martinolessio on 14/03/15.
 */
public class IOHelper {

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    private static String TAG = "IOHelper";

    public static final int IO_PERMISSIONS_READ_STORAGE_FOR_LOADING = 1;
    public static final int IO_PERMISSIONS_WRITE_STORAGE_FOR_SAVINGAS = 2;
    public static final int IO_PERMISSIONS_WRITE_STORAGE_FOR_SAVING = 3;

    public static final int IO_PERMISSIONS_GENERIC_REQUEST = 99;

    public static final String[] IO_REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.GET_ACCOUNTS
    };

    public static String CONFIG_BASE_DIR = "";

    public static Boolean canGoImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return true;
        return false;
    }

    public static int mod(int x, int y) {
        int result = x % y;
        return result < 0 ? result + y : result;
    }

    //Unused yet
    public static ArrayList<IOInfoObject> getInstalledApps(boolean getSysPackages, Context context) {
        ArrayList<IOInfoObject> res = new ArrayList<IOInfoObject>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue;
            }
            IOInfoObject newInfo = new IOInfoObject();
            newInfo.appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
            newInfo.pname = p.packageName;
            newInfo.versionName = p.versionName;
            newInfo.versionCode = p.versionCode;
            newInfo.icon = p.applicationInfo.loadIcon(context.getPackageManager());
            res.add(newInfo);
        }
        return res;
    }


    public static boolean checkForRequiredPermissions(Activity targetActivity) {

        List<String> neededPermissions = new ArrayList<>();

        for (String permission : IO_REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(targetActivity, permission) != PackageManager.PERMISSION_GRANTED)
                neededPermissions.add(permission);
        }

        Log.d(TAG, Arrays.toString(neededPermissions.toArray()));

        if (!neededPermissions.isEmpty()) {
            String[] params = neededPermissions.toArray(new String[neededPermissions.size()]);

            ActivityCompat.requestPermissions(targetActivity, params, IO_PERMISSIONS_GENERIC_REQUEST);

            // No permissions, wait if needed...
            return false;
        } else {
            // All ok guys!
            return true;
        }

    }

    public static Orientation getOrientation(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if (size.x > size.y) return Orientation.HORIZONTAL;
        return Orientation.VERTICAL;
    }

/*
 *
 * Zips a file at a location and places the resulting zip file at the toLocation
 * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
 */

    public static boolean exportBoard() {
        final int BUFFER = 2048;

        File sourceFile = new File(IOHelper.CONFIG_BASE_DIR);
        new File(Environment.getExternalStorageDirectory() + File.separator + IOApplication.APPLICATION_NAME + File.separator + "exports").mkdirs();

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + IOApplication.APPLICATION_NAME + File.separator + "exports", sourceFile.getName() + ".iziozi"));
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourceFile);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(IOHelper.CONFIG_BASE_DIR));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

/*
 *
 * Zips a subfolder
 *
 */

    private static void zipSubFolder(ZipOutputStream out, File folder,
                                     int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {

            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }


    public static void unzip(File zipFile, File destinationFolder) {
        try {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                File f = new File(destinationFolder, ze.getName());

                if (!f.isDirectory())
                {
                    if (f.getParentFile() != null || ze.isDirectory()) {
                        f.getParentFile().mkdirs();
                    }
                    f.createNewFile();
                }

                if (!ze.isDirectory()) {
                    FileOutputStream fout = new FileOutputStream(new File(destinationFolder, ze.getName()));
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }


            }

            zin.close();

        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }

    }
}

