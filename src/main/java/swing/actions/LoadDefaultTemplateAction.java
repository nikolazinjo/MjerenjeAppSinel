package swing.actions;

import data.excel.ExcelDataManager;
import data.excel.ExcelDataManagerImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.Objects;

public class LoadDefaultTemplateAction extends AbstractAction {

    public static final String DEFAULT_FILE_NAME = "MjerenjeTemplate.xls";
    private ApplicationFrame frame;

    public LoadDefaultTemplateAction(String name, ApplicationFrame frame) {
        super(name);
        Objects.requireNonNull(frame);
        this.frame = frame;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        ExcelDataManager dm = frame.getExcelDataManager();
        if (dm != null && dm.isFileModified()) {
            frame.getActions().get(WriteResultsAction.class).actionPerformed(e);
        }

        Runnable job = new Runnable() {
            @Override
            public void run() {
                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME)) {
                    ExcelDataManager dataManager = new ExcelDataManagerImpl(new HSSFWorkbook(inputStream), frame.getNotAllowedSheets());
                    frame.setExcelDataManager(dataManager);
                } catch (final Exception ex) {
                    Runnable swingJob = new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(frame,
                                    "Error occurred: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    };

                    SwingUtilities.invokeLater(swingJob);
                }
            }
        };

        Thread thread = new Thread(job);
        thread.start();
    }
}
