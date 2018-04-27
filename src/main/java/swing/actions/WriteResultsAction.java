package swing.actions;

import data.excel.ExcelDataManager;
import swing.actions.jobs.LoadingJob;
import swing.actions.jobs.WritingJob;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class WriteResultsAction extends AbstractAction {

    private ApplicationFrame frame;

    public WriteResultsAction(String name, ApplicationFrame frame) {
        super(name);
        Objects.requireNonNull(frame);
        this.frame = frame;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("'Mjerenja'yyyyMMdd-hhmmss'.xls'");
        fileChooser.setSelectedFile(new File(format.format(date)));

        int result = fileChooser.showSaveDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final ExcelDataManager dm = frame.getExcelDataManager();
        if (dm == null) {
            JOptionPane.showMessageDialog(frame,
                    "There is no opened Workbook data to save.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!dm.isFileModified()) {
            int res = JOptionPane.showConfirmDialog(frame,
                    "There is no new collected data written to opened Workbook which can be saved. " +
                            "Do you want to proceed anyway?",
                    "Not found new collected data",
                    JOptionPane.YES_NO_OPTION);
            if (res != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final Path selected = fileChooser.getSelectedFile().toPath();

        Runnable reloadAfterWriting = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread thread = new Thread(new WritingJob(frame, selected));
                    thread.start();

                    // waits for thread to finish her job.
                    thread.join();
                } catch (InterruptedException ignorable) {
                }

                new Thread(new LoadingJob(frame, null, true)).start();
            }
        };

        new Thread(reloadAfterWriting).start();
    }

}
