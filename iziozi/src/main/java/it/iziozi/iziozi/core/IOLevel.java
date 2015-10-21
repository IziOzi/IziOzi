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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martinolessio on 05/11/14.
 */

@Root(name = "IOLevel")
public class IOLevel {

    private static final String LOG_TAG = "IOLevel_DEBUG";

    @ElementList(inline = true, required = false)
    private List<IOBoard> mLevel = new ArrayList<IOBoard>();

    private int activeIndex = 0;

    public IOLevel() {

    }

    public IOBoard getBoardAtIndex(int index) {
        if (index < mLevel.size())
            return mLevel.get(index);

        return null;
    }

    public int getLevelSize() {
        if(mLevel.size() == 0)
            mLevel.add(new IOBoard());
        return mLevel.size();
    }

    public List<IOBoard> getInnerBoards() {
        return mLevel;
    }

    public IOBoard getInnerBoardAtIndex(int index)
    {
        if(index < mLevel.size())
            return mLevel.get(index);
        return null;
    }

    public void addInnerBoard(IOBoard board)
    {
        mLevel.add(board);
    }

    public void addInnerBoardAtIndex(IOBoard board, int index)
    {
        mLevel.add(Math.min(index, mLevel.size()), board);
    }

    public void removeBoardAtIndex(int index)
    {
        mLevel.remove(Math.min(index, mLevel.size()));
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }
}
