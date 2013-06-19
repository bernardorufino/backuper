package br.com.bernardorufino.labs.backuper.view;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.model.backup.Backup;
import br.com.bernardorufino.labs.backuper.model.tree.Node;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Map;

import static br.com.bernardorufino.labs.backuper.model.tree.Node.Status;

public class HistoryAdapter extends AbstractTableModel {
    private List<Backup> backups;
    private String[] columnNames;

    public HistoryAdapter(List<Backup> backups) {
        this.backups = backups;
        this.columnNames = new String[] {
            "Versão", "C", "M", "D"
        };
    }

    public void update(List<Backup> backups) {
        this.backups = backups;
    }

    public int getRowCount() {
        return backups.size();
    }

    public int getColumnCount() {
        return 4;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Backup backup = backups.get(rowIndex);
        Map<Status,Integer> summary = backup.getSummary();
        switch (columnIndex) {
            case 0:
                return Definitions.DATE_FORMATTER.print(backup.getDate());
            case 1:
                return summary.get(Status.Create);
            case 2:
                return summary.get(Status.Modify);
            case 3:
                return summary.get(Status.Delete);
        }
        return null;
    }

    public String getID(int row) {
        return backups.get(row).id;
    }

}
