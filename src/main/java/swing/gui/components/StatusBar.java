package swing.gui.components;

import comm.CommListener;
import comm.CommStatusEvents;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class StatusBar extends JPanel implements CommListener, SheetBarListener {

    public static final Color GREEN_COLOR = new Color(2, 124, 2);
    public static final Color RED_COLOR = new Color(124, 2, 2);
    public static final Color WARNING_COLOR = new Color(214, 95, 18);

    private Clock clock;
    private final JLabel statusLabel;
    private final JLabel sheetLabel;

    public StatusBar() {
        setLayout(new GridLayout(1, 3, 5, 5));
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        setBorder(border);

        statusLabel = new JLabel();
        sheetLabel = new JLabel();
        JLabel clockLabel = new JLabel();
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        add(statusLabel);
        add(sheetLabel);
        add(clockLabel);

        clock = new Clock(clockLabel, "dd/MM/yyyy hh:mm:ss");
        clock.start();

        commEvent(CommStatusEvents.DISCONNECTED, "Disconnected", "");
    }

    public void stopClock() {
        clock.stopClock();
    }

    @Override
    public void commEvent(CommStatusEvents event, String status, String shortDesc) {
        if (shortDesc.isEmpty()) {
            statusLabel.setText(String.format("STATUS: %s", status));
        } else {
            statusLabel.setText(String.format("STATUS: %s - %s", status, shortDesc));
        }

        if (event == CommStatusEvents.DISCONNECTED) {
            statusLabel.setForeground(RED_COLOR);
        } else {
            statusLabel.setForeground(GREEN_COLOR);
        }
    }

    @Override
    public void writingStarted(String sheetName) {
        sheetLabel.setText("Writing data to " + sheetName);
        sheetLabel.setForeground(GREEN_COLOR);
    }

    @Override
    public void writingStopped(String sheetName) {
        sheetLabel.setText("Data writing stopped to " + sheetName);
        sheetLabel.setForeground(RED_COLOR);
    }

    @Override
    public void dataCleared(String sheetName) {
        sheetLabel.setText("Data cleared from " + sheetName);
        sheetLabel.setForeground(WARNING_COLOR);
    }

    private static class Clock extends Thread {

        private final Runnable job = new SwingJob();
        private final static int DELAY = 1000;
        private final JLabel label;
        private final SimpleDateFormat formatter;
        private volatile boolean running = true;

        public Clock(JLabel label, String format) {
            Objects.requireNonNull(label);
            Objects.requireNonNull(format);
            this.label = label;
            this.formatter = new SimpleDateFormat(format);
        }

        public void stopClock() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                SwingUtilities.invokeLater(job);
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private  class SwingJob implements Runnable {
            @Override
            public void run() {
                Date dateTime = new Date(System.currentTimeMillis());
                label.setText(formatter.format(dateTime));
            }
        }
    }
}
