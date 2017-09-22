package data.excel;

import comm.DataListener;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ExcelDataManager extends DataListener {

    Map<String, List<String>> getData();

    List<String> getSheets();

    void selectWorkingSheet(String sheetName);

    void resetSheetColumnData();

    void writeCollectedData(Path outputFile);

    boolean isFileModified();

    void addExcelListener(ExcelListener listener);

    void removeExcelListener(ExcelListener listener);

}
