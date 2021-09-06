package util.table.logic;

import bookmark.model.txn.Bookmark;
import util.table.model.Table;

import java.util.ArrayList;
import java.util.List;

public class GrafanaDTO {
    public static Table bookmarkRecordsToTable(List<Bookmark> bookmarks) {
        if (bookmarks.size() > 0) {
            Bookmark sampleBookmark = bookmarks.get(0);
            Integer metricCount = 0;
            if (sampleBookmark != null) {
                metricCount = sampleBookmark.size();
            }
            Integer columnNum = metricCount + 1;
            Table table = new Table(columnNum);
            String[] headers = new String[columnNum];
            int i = 0;
            headers[i++] = "timestamp";
            List<String> metricNames = new ArrayList<>();
            if (metricCount > 0) {
                for (String header : sampleBookmark.keySet()) {
                    headers[i++] = header;
                    metricNames.add(header);
                }
            }
            table.setHeaders(headers);
            for (Bookmark bookmark : bookmarks) {
                int j = 0;
                Object[] row = new Object[columnNum];
                row[j++] = bookmark.getTimestamp().mysqlString();
                for (String metricName : metricNames) {
                    row[j++] = bookmark.get(metricName);
                }
                table.addRow(row);
            }
            return table;
        } else {
            return null;
        }
    }

    public static Table stateToTable(Integer state) {
        Table table = new Table(1);
        table.setHeaders(new String[]{"state"});
        table.addRow(new Object[]{state});
        return table;
    }
}
