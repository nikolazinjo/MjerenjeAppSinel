package swing.gui.components;

public interface SheetBarListener {

    void writingStarted(String sheetName);

    void writingStopped(String sheetName);

    void dataCleared(String sheetName);

}
