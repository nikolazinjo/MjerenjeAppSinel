package swing.actions;

import data.excel.ExcelDataManager;
import data.excel.ExcelDataManagerImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import swing.actions.jobs.LoadingJob;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.Objects;

public class LoadDefaultTemplateAction extends AbstractAction {

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

        LoadingJob loadingJob = new LoadingJob(frame, null, true);
        new Thread(loadingJob).start();
    }
}
