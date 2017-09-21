package swing.actions;

import comm.serial.RS232CommProvider;
import jssc.SerialPortException;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class ConnectAction extends AbstractAction {

    private ApplicationFrame frame;

    public ConnectAction(String name, ApplicationFrame frame) {
        super(name);
        Objects.requireNonNull(frame);
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RS232CommProvider provider = frame.getCommunicationProvider();
        if (provider.isConnected()) {
            JOptionPane.showMessageDialog(frame,
                    "Already connected to device on '" + provider.serialPortName() + "'serial port.",
                    "Device connected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }


        String[] ports = provider.getAvailablePorts();
        String selected = (String) JOptionPane.showInputDialog(frame,
                "Select serial port:",
                "Connect device",
                JOptionPane.PLAIN_MESSAGE,
                null,
                ports,
                ports.length != 0 ? ports[0] : "None");

        if (selected != null) {
            try {
                provider.connect(selected);
            } catch (SerialPortException e1) {
                JOptionPane.showMessageDialog(frame,
                        "Can't connect to serial port '" + selected + "'!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

}
