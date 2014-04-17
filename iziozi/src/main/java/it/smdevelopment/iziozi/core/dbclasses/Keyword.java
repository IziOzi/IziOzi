package it.smdevelopment.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "keywords")
public class Keyword   {

    @DatabaseField(id = true, columnName = "id")
    private Integer id;

    @DatabaseField(columnName = "type_id")
    private Integer typeId;

    @DatabaseField(columnName = "pictogram_id")
    private Integer pictogramId;


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
