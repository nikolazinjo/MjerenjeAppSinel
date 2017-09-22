package swing.actions.jobs;

import data.excel.ExcelDataManager;
import swing.gui.ApplicationFrame;
import swing.gui.components.StatusBar;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Objects;

public class WritingJob implements Runnable {

    private ApplicationFrame frame;
    private Path filePath;

    public WritingJob(ApplicationFrame frame, Path filePath) {
        Objects.requireNonNull(frame);
        Objects.requireNonNull(filePath);
        this.frame = frame;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            ExcelDataManager dataManager = frame.getExcelDataManager();
            if (dataManager == null) {
                return;
            }

            dataManager.writeCollectedData(filePath);
            frame.getStatusBar().fireDataStatus("Data saved", StatusBar.GREEN_COLOR);
        } catch (final Exception ex) {
            Runnable swingJob = new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(frame, "Error has occurred: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            };

            SwingUtilities.invokeLater(swingJob);
            return;
        }

        Runnable messageOk = new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(frame,
                        "File '" + filePath.getFileName() + "' successfully saved!",
                        "File saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };

        SwingUtilities.invokeLater(messageOk);
    }
}
