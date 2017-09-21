package data.excel;

public interface ExcelListener {

    void valueChanged(String sheetName, int row, int coll, Object data);

}
