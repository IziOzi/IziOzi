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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martinolessio on 07/04/14.
 *
 * Main Mappings: (Key) -> (Value Type)
 *
 *      (ROWS_NUM) -> (Integer)
 *      (COLS_NUM) -> (Integer)
 *
 */

@Root(name = "IOBoard")
public class IOBoard {

    @Attribute
    @SerializedName("rows")
    @Expose
    private Integer rows = 2;

    @Attribute
    @SerializedName("cols")
    @Expose
    private Integer cols = 3;

    @ElementList(inline = true, required = false)
    @SerializedName("buttons")
    @Expose
    private List<IOSpeakableImageButton> mButtons = new ArrayList<IOSpeakableImageButton>();;

    public IOBoard() {
    }

    public List<IOSpeakableImageButton> getButtons() {
        return mButtons;
    }

    public void setButtons(List<IOSpeakableImageButton> mButtons) {
        this.mButtons = mButtons;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer mRows) {
        this.rows = mRows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols(Integer mCols) {
        this.cols = mCols;
    }

}
