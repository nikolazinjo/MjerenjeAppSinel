package swing.gui;

import comm.DataListener;
import comm.serial.RS232CommDataProvider;
import comm.serial.RS232CommProvider;
import data.Utils;
import data.excel.ExcelDataManager;
import swing.actions.*;
import swing.gui.components.LogTable;
import swing.gui.components.SheetBar;
import swing.gui.components.SheetBarListener;
import swing.gui.components.StatusBar;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ApplicationFrame extends JFrame {

    private List<String> notAllowedSheets;
    private final RS232CommProvider communicationProvider;
    private final JTextArea area = new JTextArea();
    private SheetBarListenerImpl sheetBarListenerImpl;
    private ExcelDataManager excelDataManager;
    private SheetBar sheetBar;
    private StatusBar statusBar;
    private LogTable logTable;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorable) {
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ApplicationFrame("Measurement Wizard");
            }
        });
    }

    public ApplicationFrame(String title) {
        super(title);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);
        //setIconImage(Utils.loadImage("icon.png", 16, -1));

        try {
            notAllowedSheets = Utils.readResourceFile("NotAllowedSheetNames");
        } catch (IOException ex) {
            notAllowedSheets = new ArrayList<>();
        }

        communicationProvider = new RS232CommDataProvider();
        sheetBarListenerImpl = new SheetBarListenerImpl();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actions.get(ExitAction.class).actionPerformed(new ActionEvent(e, 0, ""));
            }

            @Override
            public void windowClosed(WindowEvent e) {
                statusBar.stopClock();
            }
        });


        initGui();
        setVisible(true);
    }

    private void initGui() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu file = new JMenu("File");
        file.add(actions.get(LoadTemplateAction.class));
        file.add(actions.get(LoadDefaultTemplateAction.class));
        file.add(actions.get(WriteResultsAction.class));
        file.add(actions.get(ExitAction.class));
        menuBar.add(file);

        JMenu comm = new JMenu("Communication");
        comm.add(actions.get(ConnectAction.class));
        comm.add(actions.get(DisconnectAction.class));
        menuBar.add(comm);

        sheetBar = new SheetBar(this);
        add(sheetBar, BorderLayout.NORTH);

        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);


        // add listeners
        sheetBar.addListener(sheetBarListenerImpl);
        sheetBar.addListener(statusBar);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);

        // botom section
        area.setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollPaneDown = new JScrollPane(area);
        scrollPaneDown.setBorder(
                BorderFactory.createTitledBorder(scrollPaneDown.getBorder(),
                        "Result data", TitledBorder.LEFT, TitledBorder.TOP));

        // top section
        logTable = new LogTable();

        // add top and bottom sections
        splitPane.setBottomComponent(scrollPaneDown);
        splitPane.setTopComponent(logTable);
        splitPane.setResizeWeight(0.7d);


        communicationProvider.addCommListener(statusBar);
        communicationProvider.addCommListener(sheetBar);
        communicationProvider.addDataListener(new DataListener() {
            @Override
            public void consumeData(String data) {
                area.append(String.format("%s%n", data));
            }
        });


    }


    public RS232CommProvider getCommunicationProvider() {
        return communicationProvider;
    }


    public LogTable getLogTable() {
        return logTable;
    }

    public Map<Class<? extends Action>, Action> getActions() {
        return actions;
    }

    public JTextArea getArea() {
        return area;
    }

    private Map<Class<? extends Action>, Action> actions;

    {
        Map<Class<? extends Action>, Action> tmp = new HashMap<>();
        tmp.put(ConnectAction.class, new ConnectAction("Connect", this));
        tmp.put(DisconnectAction.class, new DisconnectAction("Disconnect", this));
        tmp.put(ExitAction.class, new ExitAction("Exit", this));
        tmp.put(LoadDefaultTemplateAction.class, new LoadDefaultTemplateAction("Load template (default)", this));
        tmp.put(LoadTemplateAction.class, new LoadTemplateAction("Load template", this));
        tmp.put(WriteResultsAction.class, new WriteResultsAction("Write results", this));

        actions = Collections.unmodifiableMap(tmp);
    }


    public void setExcelDataManager(ExcelDataManager excelDataManager) {
        if (excelDataManager != null) {
            if (this.excelDataManager != null) {
                this.excelDataManager.removeExcelListener(logTable);
            }

            excelDataManager.addExcelListener(logTable);
            sheetBar.setSheetNames(excelDataManager.getSheets());
            logTable.loadTables(excelDataManager.getData());
        } else {
            sheetBar.setSheetNames(Collections.EMPTY_LIST);
        }

        this.excelDataManager = excelDataManager;
    }

    public ExcelDataManager getExcelDataManager() {
        return excelDataManager;
    }

    public List<String> getNotAllowedSheets() {
        return notAllowedSheets;
    }

    private class SheetBarListenerImpl implements SheetBarListener {

        @Override
        public void writingStarted(String sheetName) {
            if (excelDataManager != null) {
                communicationProvider.addDataListener(excelDataManager);
                excelDataManager.selectWorkingSheet(sheetName);
                area.setForeground(StatusBar.GREEN_COLOR);

                getActions().get(LoadTemplateAction.class).setEnabled(false);
                getActions().get(WriteResultsAction.class).setEnabled(false);
                getActions().get(LoadDefaultTemplateAction.class).setEnabled(false);
            }
        }

        @Override
        public void writingStopped(String sheetName) {
            if (excelDataManager != null) {
                communicationProvider.removeDataListener(excelDataManager);

                getActions().get(WriteResultsAction.class).setEnabled(true);
            }
            getActions().get(LoadDefaultTemplateAction.class).setEnabled(true);
            getActions().get(LoadTemplateAction.class).setEnabled(true);


            area.setForeground(StatusBar.RED_COLOR);
        }

        @Override
        public void dataCleared(String sheetName) {
            if (excelDataManager != null) {
                excelDataManager.resetSheetColumnData();
            }
        }
    }
}
