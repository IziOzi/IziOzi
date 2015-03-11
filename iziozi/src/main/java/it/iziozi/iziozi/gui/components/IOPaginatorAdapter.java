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

package it.iziozi.iziozi.gui.components;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import it.iziozi.iziozi.core.IOLevel;
import it.iziozi.iziozi.gui.IOBoardFragment;

;

/**
 * Created by martinolessio on 05/11/14.
 */
public class IOPaginatorAdapter extends FragmentStatePagerAdapter {

    private IOLevel mLevel = null;

    public IOPaginatorAdapter(FragmentManager fm, IOLevel level) {
        super(fm);
        mLevel = level;
    }

    @Override
    public Fragment getItem(int i) {
        return IOBoardFragment.newInstance(mLevel.getBoardAtIndex(i), mLevel, 0, i);
    }

    @Override
    public int getCount() {
        if(mLevel == null)
            return 0;
        return mLevel.getLevelSize();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
