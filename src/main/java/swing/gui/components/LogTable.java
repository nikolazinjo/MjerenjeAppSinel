package swing.gui.components;

import data.excel.ExcelListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogTable extends JTabbedPane implements ExcelListener {

    private static final String[] columnNames = new String[]{"Impulse", "Time", "Value"};
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    private Map<String, JTable> tableMap = new LinkedHashMap<>();

    public LogTable() {
    }

    public void loadTables(final Map<String, List<String>> sheetData) {
        tableMap.clear();

        Runnable setTables = new Runnable() {
            @Override
            public void run() {
                removeAll();

                for (Map.Entry<String, List<String>> sheet : sheetData.entrySet()) {
                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                    List<String> values = sheet.getValue();
                    for (int i = 0, n = values.size(); i < n; i++) {
                        tableModel.addRow(new Object[]{i + 1, "<template value>", values.get(i)});
                    }

                    JTable table = new JTable(tableModel);
                    addTab(sheet.getKey(), new JScrollPane(table));
                    tableMap.put(sheet.getKey(), table);
                }
            }
        };

        SwingUtilities.invokeLater(setTables);
    }

    @Override
    public void valueChanged(final String sheetName,final int row,final int coll,final Object data) {
        if (!tableMap.containsKey(sheetName)) {
            return;
        }

        JTable table = tableMap.get(sheetName);
        final DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable swingJob = new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    Date date = new Date(System.currentTimeMillis());
                    model.addRow(new Object[]{row, format.format(date), data});
                } else if (!isEmpty(sheetName)) {
                    model.removeRow(row);
                }
            }
        };

        SwingUtilities.invokeLater(swingJob);
    }

    public boolean isEmpty(String sheetName) {
        return tableMap.get(sheetName).getModel().getRowCount() == 0;
    }

    public void setSelected(String sheetName) {
        int index = findIndex(sheetName);
        if (index != -1) {
            super.setSelectedIndex(index);
        }
    }

    private int findIndex(String sheetName) {
        for (int i = 0, n = getTabCount(); i < n; i++) {
            if (getTitleAt(i).equals(sheetName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setSelectedIndex(int index) {
       // prevent selection with mouse
    }
}
