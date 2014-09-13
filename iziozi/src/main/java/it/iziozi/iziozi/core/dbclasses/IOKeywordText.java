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

package it.iziozi.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "keywordtexts")
public class IOKeywordText {

    public static final String ID_NAME = "id";
    public static final String KEYWORD_ID_NAME = "keyword_id";
    public static final String LANGUAGE_ID_NAME = "language_id";
    public static final String TEXT_NAME = "text";

    @DatabaseField(id = true, columnName = "id")
    private Integer id;

    @DatabaseField(columnName = "keyword_id")
    private Integer keywordId;

    @DatabaseField(columnName = "language_id")
    private Integer languageId;

    @DatabaseField(columnName = "text")
    private String text;

    @DatabaseField(foreign = true)
    private IOKeyword keyword;

    @DatabaseField(foreign = true)
    private IOLanguage language;


    public IOKeywordText() {
    }

    public IOKeywordText(Integer id, Integer keywordId, Integer languageId, String text) {
        this.id = id;
        this.keywordId = keywordId;
        this.languageId = languageId;
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(Integer keywordId) {
        this.keywordId = keywordId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
