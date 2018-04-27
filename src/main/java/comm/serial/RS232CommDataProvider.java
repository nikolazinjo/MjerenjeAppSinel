package comm.serial;

import comm.CommListener;
import comm.CommStatusEvents;
import comm.DataListener;
import jssc.*;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.List;

public class RS232CommDataProvider implements RS232CommProvider {

    private List<DataListener> dataListeners = new ArrayList<>();
    private List<CommListener> commListeners = new ArrayList<>();
    private SerialPort serialPort;


    @Override
    public void connect(String serialPortName) throws SerialPortException {
        disconnect();

        serialPort = new SerialPort(serialPortName);
        serialPort.openPort();
        serialPort.setParams(
                SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        serialPort.addEventListener(new RS232Listener());
        notifyCommListeners(
                CommStatusEvents.CONNECTED,
                "Connected",
                "Listening on port " + serialPort.getPortName());
    }

    @Override
    public void disconnect() throws SerialPortException {
        if (serialPort != null && serialPort.isOpened()) {
            serialPort.closePort();

            notifyCommListeners(
                    CommStatusEvents.DISCONNECTED,
                    "Disconnected",
                    "");
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null ? serialPort.isOpened() : false;
    }

    @Override
    public String serialPortName() {
        return serialPort != null ? serialPort.getPortName() : null;
    }

    @Override
    public String[] getAvailablePorts() {
        return SerialPortList.getPortNames();
    }

    @Override
    public void addDataListener(DataListener listener) {
        dataListeners.add(listener);
    }

    @Override
    public void removeDataListener(DataListener listener) {
        dataListeners.remove(listener);
    }

    @Override
    public void addCommListener(CommListener listener) {
        commListeners.add(listener);
    }

    @Override
    public void removeCommListener(CommListener listener) {
        commListeners.remove(listener);
    }

    private void notifyListeners(String data) {
        for (DataListener listener : dataListeners) {
            listener.consumeData(data);
        }
    }

    private void notifyCommListeners(CommStatusEvents status, String statusMessage, String description) {
        for (CommListener listener : commListeners) {
            listener.commEvent(status, statusMessage, description);
        }
    }


    private class RS232Listener implements SerialPortEventListener {

        CharArrayWriter streamBuffer = new CharArrayWriter();
        boolean matchedCR;

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (!serialPortEvent.isRXCHAR()) {
                return;
            }

            try {
                byte[] array = serialPort.readBytes();
                if (array == null) {
                    return;
                }

                for (byte b : array) {

                    switch (b) {
                        case '\r':
                            matchedCR = true;
                            break;

                        case '\n':
                            if (matchedCR) {
                                try {
                                    String data = String.valueOf(streamBuffer.toCharArray());
                                    notifyListeners(data);
                                } catch (NumberFormatException ignorable) {
                                }

                                streamBuffer.reset();
                                matchedCR = false;
                                break;
                            }

                        default:
                            matchedCR = false;
                            streamBuffer.write(b);
                    }
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
