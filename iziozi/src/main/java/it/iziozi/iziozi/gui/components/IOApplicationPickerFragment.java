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

package it.iziozi.iziozi.gui.components;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.iziozi.iziozi.R;

/**
 * Created by martinolessio on 11/04/15.
 */
public class IOApplicationPickerFragment extends ListFragment {

    private final static String LOG_TAG = "IOApplicationPickerFragment";
    private List<ResolveInfo> applicationList = new ArrayList<ResolveInfo>();

    private IOApplicationSelectionListener mListener;

    public static IOApplicationPickerFragment getInstance(IOApplicationSelectionListener listener)
    {
        IOApplicationPickerFragment fragment = new IOApplicationPickerFragment();
        fragment.setListener(listener);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupAdapter();

    }

    public void setListener(IOApplicationSelectionListener mListener) {
        this.mListener = mListener;
    }

    private void setupAdapter() {

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);

        applicationList.addAll(pkgAppsList);

        setListAdapter(new IOApplicationListAdapter(getActivity(), R.layout.application_list_item, pkgAppsList));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mListener.onApplicationSelected(applicationList.get(position));
    }

    private class IOApplicationListAdapter extends ArrayAdapter<ResolveInfo> {


        public IOApplicationListAdapter(Context context, int resource, List<ResolveInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (null == convertView)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.application_list_item, parent, false);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.image_view);
            TextView nameText = (TextView) convertView.findViewById(R.id.name_text);
            TextView descriptionText = (TextView) convertView.findViewById(R.id.description_text);

            ResolveInfo item = getItem(position);

            nameText.setText(item.loadLabel(getActivity().getPackageManager()));
            imageView.setImageDrawable(item.loadIcon(getActivity().getPackageManager()));
            descriptionText.setText(item.resolvePackageName);

            return convertView;
        }
    }

    public interface IOApplicationSelectionListener{
        public void onApplicationSelected(ResolveInfo resolveInfo);
    }

}
