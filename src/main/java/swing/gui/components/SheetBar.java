package swing.gui.components;

import comm.CommListener;
import comm.CommStatusEvents;
import data.excel.ExcelDataManager;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SheetBar extends JPanel implements SheetBarManager, CommListener {

    private List<SheetBarListener> listeners = new ArrayList<>();

    private final JButton start;
    private final JComboBox<String> chooser;
    private ApplicationFrame frame;
    private CommStatusEvents status;

    public SheetBar(final ApplicationFrame frame) {
        Objects.requireNonNull(frame);
        this.frame = frame;

        Border border = BorderFactory.createTitledBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED),
                "Recording options", TitledBorder.LEFT, TitledBorder.TOP);
        setBorder(border);

        JLabel sheet = new JLabel("Destination sheet:");
        add(sheet);

        start = new JButton("Start");
        final JButton stop = new JButton("Stop");
        final JButton reset = new JButton("Reset");
        chooser = new JComboBox<>(new MyComboBoxModel());
        chooser.setPreferredSize(new Dimension(250, 20));

        add(chooser);
        add(start);
        add(stop);
        add(reset);

        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                String item = (String) comboBox.getSelectedItem();
                if (item != null) {

                    ExcelDataManager dataManager = frame.getExcelDataManager();
                    if (dataManager != null) {
                        dataManager.selectWorkingSheet(item);
                        frame.getLogTable().setSelected(item);
                    }

                    start.setEnabled(true);
                    if (!frame.getLogTable().isEmpty(item)) {
                        reset.setEnabled(true);
                    }
                }
            }
        });

        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sheetName = (String) chooser.getSelectedItem();
                if (sheetName == null) {
                    return;
                }

                if (status != CommStatusEvents.CONNECTED) {
                    JOptionPane.showMessageDialog(frame, "Device is not connected!",
                            "Can't start", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                chooser.setSelectedIndex(chooser.getSelectedIndex());

                for (SheetBarListener listener : listeners) {
                    listener.writingStarted(sheetName);
                }

                chooser.setEnabled(false);
                start.setEnabled(false);
                stop.setEnabled(true);
                reset.setEnabled(false);
            }
        });

        stop.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sheetName = (String) chooser.getSelectedItem();
                if (sheetName == null) {
                    return;
                }

                for (SheetBarListener listener : listeners) {
                    listener.writingStopped(sheetName);
                }

                chooser.setEnabled(true);
                start.setEnabled(true);
                stop.setEnabled(false);
                reset.setEnabled(true);
            }
        });

        reset.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sheetName = (String) chooser.getSelectedItem();
                if (sheetName == null) {
                    return;
                }

                for (SheetBarListener listener : listeners) {
                    listener.dataCleared(sheetName);
                }

                chooser.setEnabled(true);
                start.setEnabled(true);
                stop.setEnabled(false);
                reset.setEnabled(false);
            }
        });

        chooser.setEnabled(true);
        start.setEnabled(false);
        stop.setEnabled(false);
        reset.setEnabled(false);
    }

    public void setSheetNames(List<String> list) {
        ((MyComboBoxModel) chooser.getModel()).setData(list);
    }

    @Override
    public void addListener(SheetBarListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(SheetBarListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void commEvent(final CommStatusEvents event, final String status, final String shortDesc) {
        this.status = event;
    }


    private static class MyComboBoxModel implements ComboBoxModel<String> {

        private List<String> sheetList = new ArrayList<>();
        private List<ListDataListener> listeners = new ArrayList<>();
        private int selected = -1;

        public void setData(List<String> list) {
            int size = getSize();
            sheetList.clear();
            for (ListDataListener listener : listeners) {
                listener.intervalRemoved(
                        new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size));
            }

            sheetList.addAll(list);
            for (ListDataListener listener : listeners) {
                listener.intervalAdded(
                        new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, getSize()));
            }
        }

        @Override
        public Object getSelectedItem() {
            return (selected < 0 || selected >= getSize()) ? null : sheetList.get(selected);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            int index = sheetList.indexOf(anItem);
            if (index < 0) {
                selected = -1;
            } else {
                selected = index;
            }

            for (ListDataListener listener : listeners) {
                listener.contentsChanged(
                        new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1));
            }
        }

        @Override
        public int getSize() {
            return sheetList.size();
        }

        @Override
        public String getElementAt(int index) {
            return sheetList.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }
}
