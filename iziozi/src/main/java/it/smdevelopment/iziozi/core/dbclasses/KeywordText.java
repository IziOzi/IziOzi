package it.smdevelopment.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "keywordtexts")
public class KeywordText {

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
    private Keyword keyword;

    @DatabaseField(foreign = true)
    private Language language;


    public KeywordText() {
    }

    public KeywordText(Integer id, Integer keywordId, Integer languageId,String text) {
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
