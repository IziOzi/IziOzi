package it.smdevelopment.iziozi.core.dbclasses;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by martinolessio on 17/04/14.
 */

@DatabaseTable(tableName = "pictograms")
public class Pictogram {

    public static final String ID_NAME = "id";
    public static final String URL_NAME = "url";
    public static final String FILE_NAME = "file";

    @DatabaseField(id = true, columnName = "id")
    private Integer id;

    @DatabaseField(columnName = "url")
    private String url;

    @DatabaseField(columnName = "file")
    private String filePath;

    public Pictogram() {
    }

    public Pictogram(Integer id, String filePath, String url) {
        this.id = id;
        this.filePath = filePath;
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
