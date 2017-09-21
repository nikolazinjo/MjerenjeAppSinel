package swing.actions;

import comm.serial.RS232CommProvider;
import jssc.SerialPortException;
import swing.gui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class DisconnectAction extends AbstractAction {

    private ApplicationFrame frame;

    public DisconnectAction(String name, ApplicationFrame frame) {
        super(name);
        Objects.requireNonNull(frame);
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RS232CommProvider provider = frame.getCommunicationProvider();
        if (!provider.isConnected()) {
            return;
        }

        try {
            provider.disconnect();
        } catch (SerialPortException ignorable1) {
        }

        JOptionPane.showMessageDialog(frame,
                "Device successfully disconnected from application.",
                "Device disconnected",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
