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

package it.iziozi.iziozi.gui.tutorial;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.iziozi.iziozi.R;


public class FragmentTutorialPage extends Fragment implements View.OnClickListener {

    private int page;
    private static final String TAG = "IziOzi";

    public interface OnTutorialFinishedListener {
        public void onTutorialFinish();
    }

    private OnTutorialFinishedListener listener;

    public static FragmentTutorialPage newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("page", page);

        FragmentTutorialPage fragmentTutorialPage = new FragmentTutorialPage();
        fragmentTutorialPage.setArguments(args);

        return fragmentTutorialPage;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            try {
                listener = (OnTutorialFinishedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity " + context.toString() + " does not implement " +
                        "OnTutorialFinishedListener!");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "Arguments to FragmentTutorialPage is null!");
        } else {
          page = args.getInt("page");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_page, container, false);
        TextView tvHeader = (TextView) v.findViewById(R.id.tv_header);
        TextView tvDetails = (TextView) v.findViewById(R.id.tv_details_1);
        TextView tvDetails2 = (TextView) v.findViewById(R.id.tv_details_2);
        ImageView imageView = (ImageView) v.findViewById(R.id.iv_image);
        ImageView imageView2 = (ImageView) v.findViewById(R.id.iv_image2);
        Button btnDone = (Button) v.findViewById(R.id.btn_finish);
        btnDone.setOnClickListener(this);

        if (page == 0 || page == 8) {
            tvHeader.setTextColor(Color.DKGRAY);
            tvHeader.setGravity(Gravity.CENTER);
            tvHeader.setBackgroundColor(Color.argb(255, 250, 250, 250));
        }
        if (page == 8) btnDone.setVisibility(View.VISIBLE);

        int textStringRes[] = getStringResource(page);
        int imgResourceId[] = getImageResource(page);

        if (textStringRes[0] != 0) tvHeader.setText(textStringRes[0]);
        if (textStringRes[1] != 0) tvDetails.setText(textStringRes[1]);
        if (textStringRes[2] != 0) tvDetails2.setText(textStringRes[2]);

        if (imgResourceId[0] != 0) imageView.setImageResource(imgResourceId[0]);
        if (imgResourceId[1] != 0) imageView2.setImageResource(imgResourceId[1]);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finish:
                // Communicate to the activity that we are done and it needs to remove this fragment
                // also make sure to set the prefs
                listener.onTutorialFinish();
                break;
        }
    }

    private int[] getStringResource(int page) {
        int stringRes[] = new int[3];
        String title = "tutorial_header_" + page;
        String details = "tutorial_details1_" + page;
        String details2 = "tutorial_details2_" + page;

        stringRes[0] = getContext().getResources().getIdentifier(title, "string", getContext().getPackageName());
        stringRes[1] = getContext().getResources().getIdentifier(details, "string", getContext().getPackageName());
        stringRes[2] = getContext().getResources().getIdentifier(details2, "string", getContext().getPackageName());

        return stringRes;
    }

    private int[] getImageResource(int page) {
        String imageName = "tutorial_image1_" + page;
        String imageName2 = "tutorial_image2_" + page;
        int res = getContext().getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
        int res2 = getContext().getResources().getIdentifier(imageName2, "drawable", getContext().getPackageName());
        int ret[] = { res, res2 };

        return ret;
    }
}
