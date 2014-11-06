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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOLevel;
import it.iziozi.iziozi.gui.components.IOPaginatorAdapter;

public class IOPaginatedBoardFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onRegisterActiveLevel(IOLevel level);
        public void onPageScrolled(int newIndex);
    }

    private OnFragmentInteractionListener mListener;

    private IOLevel mLevel = null;
    private ViewPager mViewPager = null;


    public static IOPaginatedBoardFragment newInstance(IOLevel level) {
        IOPaginatedBoardFragment fragment = new IOPaginatedBoardFragment();

        fragment.setLevel(level);

        return fragment;
    }
    public IOPaginatedBoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup mainView = (ViewGroup) inflater.inflate(R.layout.fragment_iopaginated_board, container, false);

        mViewPager = (ViewPager) mainView.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new IOPaginatorAdapter(getFragmentManager(), mLevel));

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mListener != null) {
                    mListener.onPageScrolled(position);
                }
            }
        });

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();

        if (mListener != null) {
            mListener.onRegisterActiveLevel(mLevel);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public IOLevel getLevel() {
        return mLevel;
    }

    public void setLevel(IOLevel mLevel) {
        this.mLevel = mLevel;
    }

    public void paginateLeft() {
        if (mViewPager.getCurrentItem() > 0)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    public void paginateRight() {
        if (mViewPager.getCurrentItem() < mViewPager.getAdapter().getCount() - 1)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    public void refreshView()
    {
        Log.d("trst", "adapter views " + mViewPager.getCurrentItem() + " " + mViewPager.getChildCount());
        mViewPager.setAdapter(new IOPaginatorAdapter(getFragmentManager(), mLevel));

    }
}
