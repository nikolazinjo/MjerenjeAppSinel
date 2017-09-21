package data.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import javax.swing.*;
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
    private Path inputExcelFile;


    public ExcelDataManagerImpl(HSSFWorkbook workbook, List<String> notAllowed) {
        this.hssfWorkbook = workbook;
        this.notAllowed.addAll(notAllowed);
        loadSheets();
    }

    public ExcelDataManagerImpl(Path inputExcelFile, List<String> notAllowed) {
        if (!Files.isReadable(inputExcelFile)) {
            throw new RuntimeException("File '" + inputExcelFile.getFileName() + "' can't be open opened or read!");
        }

        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(inputExcelFile))) {
            hssfWorkbook = new HSSFWorkbook(bis);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        loadSheets();
        this.inputExcelFile = inputExcelFile;
        this.notAllowed.addAll(notAllowed);
    }

    private void loadSheets() {
        for (int i = 0, n = hssfWorkbook.getNumberOfSheets(); i < n; i++) {
            HSSFSheet sheet = hssfWorkbook.getSheetAt(i);
            sheet.setForceFormulaRecalculation(true);

            if (notAllowed.contains(sheet.getSheetName())) {
                continue;
            }

            currentSheetIndexes.put(sheet.getSheetName(), START_ROW_INDEX);

            List<String> collData = new ArrayList<>();
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
    }


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
    public Path getInputExcelFile() {
        return inputExcelFile;
    }


    public void writeCollectedData(Path outputFile) {
        if (written) {
            throw new RuntimeException("Workbook already saved! Please reopen template to start new measurement.");
        }

        // write last impulse number
        for (Map.Entry<String, Integer> entry : currentSheetIndexes.entrySet()) {
            if (entry.getValue()<1) {
                continue;
            }

            HSSFSheet sheet = hssfWorkbook.getSheet(entry.getKey());
            HSSFRow row = sheet.getRow(COUNTER_ROW_INDEX);
            if (row == null) {
                row = sheet.createRow(COUNTER_ROW_INDEX);
            }

            HSSFCell cell = row.getCell(COUNTER_COLUMN_INDEX);
            if (cell == null) {
                cell = row.createCell(COUNTER_COLUMN_INDEX);
            }

            cell.setCellValue(String.valueOf(entry.getValue()-1));
        }

        // writing file to disk
        try (OutputStream os = Files.newOutputStream(outputFile)) {
            hssfWorkbook.write(os);
            hssfWorkbook.close();
            written = true;
            fileModified = false;
            listeners.clear();
        } catch (IOException e) {
            throw new RuntimeException("Workbook can't be exported to file '" + outputFile.getFileName() + "'!");
        }

    }

    @Override
    public Map<String, List<String>> getData() {
        return workbookData;
    }

    public List<String> getSheets() {
        return new ArrayList<>(currentSheetIndexes.keySet());
    }

    public void selectWorkingSheet(String sheetName) {
        int index = hssfWorkbook.getSheetIndex(sheetName);
        if (index >= 0 && index != hssfWorkbook.getActiveSheetIndex()) {
            hssfWorkbook.setActiveSheet(index);
        }
    }

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
                row.removeCell(cell);
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

        // get row
        int currentRow = currentSheetIndexes.get(sheetName);
        HSSFRow row = sheet.getRow(currentRow);
        row = (row == null) ? sheet.createRow(currentRow) : row;

        // get cell
        HSSFCell cell = row.getCell(COLUMN_INDEX);
        cell = (cell == null) ? row.createCell(COLUMN_INDEX) : cell;

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
