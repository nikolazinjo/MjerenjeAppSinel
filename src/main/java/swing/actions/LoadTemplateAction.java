package swing.actions;

import data.excel.ExcelDataManager;
import swing.actions.jobs.LoadingJob;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
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
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Microsoft Excel 97-2003 (*.xls)", "xls"));
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Microsoft Excel Open XML (*.xlsx)", "xlsx"));

        int result = fileChooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        ExcelDataManager dm = frame.getExcelDataManager();
        if (dm != null && dm.isFileModified()) {
            frame.getActions().get(WriteResultsAction.class).actionPerformed(e);
        }

        Path selected = fileChooser.getSelectedFile().toPath();
        LoadingJob loadingJob = new LoadingJob(frame, selected, false);
        new Thread(loadingJob).start();
    }
}
