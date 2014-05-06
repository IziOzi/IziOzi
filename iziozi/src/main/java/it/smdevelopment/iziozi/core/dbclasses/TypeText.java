package it.smdevelopment.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "typetexts")
public class TypeText {

    @DatabaseField(id = true, columnName = "id")
    private Integer id;

    @DatabaseField(columnName = "text")
    private String text;

    @DatabaseField(columnName = "type_id")
    private Integer typeId;

    @DatabaseField(columnName = "language_id")
    private Integer languageId;

    public TypeText() {
    }

    public TypeText(Integer id, String text, Integer typeId, Integer languageId) {
        this.id = id;
        this.text = text;
        this.typeId = typeId;
        this.languageId = languageId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }
}