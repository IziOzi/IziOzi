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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.iziozi.iziozi.R;


public class FragmentTutorialViewPager extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_viewpager, container, false);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.vp_tutorial);
        PagerTitleStrip pagerTitleStrip = (PagerTitleStrip) v.findViewById(R.id.vp_pager_title);
        viewPager.setAdapter(new FragmentTutorialAdapter(getFragmentManager(), getActivity().getApplicationContext()));

        pagerTitleStrip.setTextColor(Color.WHITE);
        pagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

        return v;
    }


    public static class FragmentTutorialAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_PAGES = 9;
        private Context context;

        public FragmentTutorialAdapter(FragmentManager manager, Context context) {
            super(manager);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            FragmentTutorialPage fragmentTutorialPage = FragmentTutorialPage.newInstance(position);

            return fragmentTutorialPage;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String resource = "page_title_" + position;
            String pageTitle = null;

            int resId = context.getResources().getIdentifier(resource, "string", context.getPackageName());
            pageTitle = context.getResources().getString(resId);

            return pageTitle;
        }

    }
}










