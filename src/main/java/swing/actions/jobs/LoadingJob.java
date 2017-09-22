package swing.actions.jobs;

import data.excel.ExcelDataManager;
import data.excel.ExcelDataManagerImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import swing.gui.ApplicationFrame;
import swing.gui.components.StatusBar;

import javax.swing.*;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

public class LoadingJob implements Runnable {

    private static final String DEFAULT_FILE_NAME = "MjerenjeTemplate.xls";
    private ApplicationFrame frame;
    private Path filePath;
    private boolean resetSheets;

    public LoadingJob(ApplicationFrame frame, Path filePath, boolean resetSheets) {
        Objects.requireNonNull(frame);
        this.frame = frame;
        this.filePath = filePath;
        this.resetSheets = resetSheets;
    }

    @Override
    public void run() {
        try {
            ExcelDataManager dataManager;
            if (filePath == null) {
                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME)) {
                    dataManager = new ExcelDataManagerImpl(new HSSFWorkbook(inputStream), frame.getNotAllowedSheets(), resetSheets);
                }
            } else {
                dataManager = new ExcelDataManagerImpl(filePath, frame.getNotAllowedSheets(), resetSheets);
            }

            frame.setExcelDataManager(dataManager);
            frame.getStatusBar().fireDataStatus("File loaded", StatusBar.GREEN_COLOR);
        } catch (final Exception ex) {
            Runnable swingJob = new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(frame, "Error occurred: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            };
            SwingUtilities.invokeLater(swingJob);
        }
    }
}
