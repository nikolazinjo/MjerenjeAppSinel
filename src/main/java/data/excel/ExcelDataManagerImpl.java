package data.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelDataManagerImpl implements ExcelDataManager {

    public static final int COUNTER_ROW_INDEX = 0;
    public static final int COUNTER_COLUMN_INDEX = 5;
    public static final int COLUMN_INDEX = 0;
    public static final int START_ROW_INDEX = 1;
    private final List<String> notAllowed = new ArrayList<>();

    private Map<String, Integer> currentSheetIndexes = new LinkedHashMap<>();
    private Map<String, List<String>> workbookData = new LinkedHashMap<>();
    private List<ExcelListener> listeners = new ArrayList<>();
    private HSSFWorkbook hssfWorkbook;
    private boolean fileModified;
    private boolean written;


    public ExcelDataManagerImpl(HSSFWorkbook workbook, List<String> notAllowed, boolean resetSheets) {
        this.hssfWorkbook = workbook;
        this.notAllowed.addAll(notAllowed);
        loadSheets(resetSheets);
    }

    public ExcelDataManagerImpl(Path inputExcelFile, List<String> notAllowed, boolean resetSheets) {
        if (!Files.isReadable(inputExcelFile)) {
            throw new RuntimeException("File '" + inputExcelFile.getFileName() + "' can't be opened for reading!");
        }

        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(inputExcelFile))) {
            hssfWorkbook = new HSSFWorkbook(bis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.notAllowed.addAll(notAllowed);
        loadSheets(resetSheets);
    }

    private void loadSheets(boolean resetSheets) {
        for (int i = 0, n = hssfWorkbook.getNumberOfSheets(); i < n; i++) {
            HSSFSheet sheet = hssfWorkbook.getSheetAt(i);
            sheet.setForceFormulaRecalculation(true);

            if (notAllowed.contains(sheet.getSheetName())) {
                continue;
            }

            currentSheetIndexes.put(sheet.getSheetName(), START_ROW_INDEX);

            List<String> collData = new ArrayList<>();
            if (resetSheets) {
                selectWorkingSheet(sheet.getSheetName());

                //reset column data
                resetSheetColumnData();
                workbookData.put(sheet.getSheetName(), collData);

                // reset counter
                HSSFCell cell = getCell(sheet, COUNTER_ROW_INDEX, COUNTER_COLUMN_INDEX);
                cell.setCellValue((String) null);
                continue;
            }

            for (int j = START_ROW_INDEX, m = sheet.getLastRowNum(); j < m; j++) {
                HSSFRow row = sheet.getRow(j);
                if (row == null) {
                    continue;
                }

                HSSFCell cell = row.getCell(COLUMN_INDEX);
                if (cell != null) {
                    if (cell.getCellTypeEnum() == CellType.STRING) {
                        String value = cell.getStringCellValue();
                        if (!value.isEmpty()) {
                            collData.add(value);
                        }
                    } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                        String value = String.valueOf(cell.getNumericCellValue());
                        if (!value.isEmpty()) {
                            collData.add(value);
                        }
                    } else {
                        collData.add("error reading");
                    }
                } else {
                    collData.add("");
                }
            }

            workbookData.put(sheet.getSheetName(), collData);
        }

        fileModified = false;
    }

    @Override
    public boolean isFileModified() {
        return fileModified;
    }

    @Override
    public void addExcelListener(ExcelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeExcelListener(ExcelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void writeCollectedData(Path outputFile) {
        if (written) {
            throw new RuntimeException("Workbook already written! Open a new template for next measurements.");
        }

        // write last impulse number
        for (Map.Entry<String, Integer> entry : currentSheetIndexes.entrySet()) {
            if (entry.getValue() < 1) {
                continue;
            }

            HSSFSheet sheet = hssfWorkbook.getSheet(entry.getKey());
            HSSFCell cell = getCell(sheet, COUNTER_ROW_INDEX, COUNTER_COLUMN_INDEX);
            cell.setCellValue(String.valueOf(entry.getValue() - 1));
        }

        // writing file to disk
        try (OutputStream os = Files.newOutputStream(outputFile)) {
            hssfWorkbook.write(os);
            hssfWorkbook.close();
            written = true;
            fileModified = false;
            listeners.clear();
        } catch (IOException e) {
            throw new RuntimeException("Can't write workbook to file '" + outputFile.getFileName() + "'!");
        }

    }

    private HSSFCell getCell(HSSFSheet sheet, int row, int coll) {
        HSSFRow excelRow = sheet.getRow(row);
        if (excelRow == null) {
            excelRow = sheet.createRow(row);
        }

        HSSFCell cell = excelRow.getCell(coll);
        if (cell == null) {
            cell = excelRow.createCell(coll);
        }

        return cell;
    }

    @Override
    public Map<String, List<String>> getData() {
        return workbookData;
    }

    @Override
    public List<String> getSheets() {
        return new ArrayList<>(currentSheetIndexes.keySet());
    }

    @Override
    public void selectWorkingSheet(String sheetName) {
        int index = hssfWorkbook.getSheetIndex(sheetName);
        if (index >= 0 && index != hssfWorkbook.getActiveSheetIndex()) {
            hssfWorkbook.setActiveSheet(index);
        }
    }

    @Override
    public void resetSheetColumnData() {
        HSSFSheet sheet = getActiveSheet();
        if (sheet == null) {
            return;
        }

        String sheetName = sheet.getSheetName();
        for (int i = START_ROW_INDEX, n = sheet.getLastRowNum(); i < n; i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            HSSFCell cell = row.getCell(COLUMN_INDEX);
            if (cell != null) {
                cell.setCellValue((String) null);
            }

            notifyListeners(sheetName, 0, COLUMN_INDEX, null);
        }

        currentSheetIndexes.put(sheet.getSheetName(), START_ROW_INDEX);
        fileModified = true;
    }


    private HSSFSheet getActiveSheet() {
        int index = hssfWorkbook.getActiveSheetIndex();
        if (index < 0) {
            return null;
        }

        return hssfWorkbook.getSheetAt(index);
    }

    @Override
    public void consumeData(String data) {
        HSSFSheet sheet = getActiveSheet();
        if (sheet == null) {
            return;
        }

        String sheetName = sheet.getSheetName();
        int currentRow = currentSheetIndexes.get(sheetName);
        HSSFCell cell = getCell(sheet, currentRow, COLUMN_INDEX);

        // write data
        cell.setCellValue(data);

        // update counter
        currentSheetIndexes.put(sheetName, currentRow + 1);
        fileModified = true;

        notifyListeners(sheetName, currentRow, COLUMN_INDEX, data);
    }

    private void notifyListeners(String sheetName, int row, int coll, Object data) {
        for (ExcelListener listener : listeners) {
            listener.valueChanged(sheetName, row, coll, data);
        }
    }
}
