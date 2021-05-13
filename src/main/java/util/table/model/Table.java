package util.table.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private Integer numColumns;
    public Column[] columns;
    public List<Object[]> rows;
    String type = "table";

    public Table(Integer numColumns){
        this.setNumColumns(numColumns);
        this.columns = new Column[numColumns];
        rows = new ArrayList<>();
    }

    public void setHeaders(String[] headers){
        for (int i = 0; i < headers.length; i++) {
            Column column = new Column();
            column.text = headers[i];
            columns[i] = column;
        }
    }

    public Column[] getColumns(){
        return columns;
    }

    public void addRow(Object[] row){
        rows.add(row);
    }

    public List<Object[]> getRows(){
        return rows;
    }

    public void setRows(List<Object[]> rows) {
        this.rows = rows;
    }

    public Integer getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(Integer numColumns) {
        this.numColumns = numColumns;
    }

    public class Column{
        public String type = "string";
        public String text;
    }
}

