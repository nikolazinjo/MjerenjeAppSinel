package comm.serial;

import comm.CommListener;
import comm.DataListener;
import jssc.SerialPortException;

public interface RS232CommProvider {

    void connect(String serialPortName) throws SerialPortException;

    void disconnect() throws SerialPortException;

    boolean isConnected();

    String serialPortName();

    String[] getAvailablePorts();

    void addDataListener(DataListener listener);

    void removeDataListener(DataListener listener);

    void addCommListener(CommListener listener);

    void removeCommListener(CommListener listener);

}
