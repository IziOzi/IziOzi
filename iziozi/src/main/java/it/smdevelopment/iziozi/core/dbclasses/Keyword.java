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

package it.smdevelopment.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "keywords")
public class Keyword   {

    public static final String ID_NAME = "id";
    public static final String TYPE_ID_NAME = "type_id";
    public static final String PICTOGRAM_ID_NAME = "pictogram_id";

    @DatabaseField(id = true, columnName = "id")
    private Integer id;

    @DatabaseField(columnName = "type_id")
    private Integer typeId;

    @DatabaseField(columnName = "pictogram_id")
    private Integer pictogramId;

    @DatabaseField(foreign = true)
    private Pictogram pictogram;


    public Keyword(){}

    public Keyword(Integer id, Integer typeId, Integer pictogramId) {
        this.id = id;
        this.typeId = typeId;
        this.pictogramId = pictogramId;
    }

    public Integer getPictogramId() {
        return pictogramId;
    }

    public void setPictogramId(Integer pictogramId) {
        this.pictogramId = pictogramId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
