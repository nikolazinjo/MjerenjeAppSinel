package swing.actions;

import data.excel.ExcelDataManager;
import data.excel.ExcelDataManagerImpl;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;

public class LoadTemplateAction extends AbstractAction {

    private ApplicationFrame frame;

    public LoadTemplateAction(String name, ApplicationFrame frame) {
        super(name);
        Objects.requireNonNull(frame);
        this.frame = frame;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();

        // filters
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Excel 97-2003 (*.xls)", "xls"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Excel Open XML (*.xlsx)", "xlsx"));

        int result = fileChooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        ExcelDataManager dm = frame.getExcelDataManager();
        if (dm != null && dm.isFileModified()) {
            frame.getActions().get(WriteResultsAction.class).actionPerformed(e);
        }

        final File selected = fileChooser.getSelectedFile();
        Runnable job = new Runnable() {
            @Override
            public void run() {
                try {
                    ExcelDataManager dataManager = new ExcelDataManagerImpl(selected.toPath());
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
