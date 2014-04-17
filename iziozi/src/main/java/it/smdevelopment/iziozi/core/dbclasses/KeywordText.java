package it.smdevelopment.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "keywordtexts")
public class KeywordText {

    @DatabaseField(id = true, columnName = "id")
    private Integer id;

    @DatabaseField(columnName = "keyword_id")
    private Integer keywordId;

    @DatabaseField(columnName = "language_id")
    private Integer languageId;

    public KeywordText() {
    }

    public KeywordText(Integer id, Integer keywordId, Integer languageId) {
        this.id = id;
        this.keywordId = keywordId;
        this.languageId = languageId;
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
}
