package comm.serial;


import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Rs232emulator {

    public static void main(String[] args) {


        SerialPort serialPort = new SerialPort("COM2");

        try {
            // open port for communication
            serialPort.openPort();

            // baundRate, numberOfDataBits, numberOfStopBits, parity
            serialPort.setParams(9600, 8, 1, 0);

            try (Scanner sc = new Scanner(System.in)) {
                String input = null;
                while (sc.hasNextLine()) {
                    input = sc.nextLine();
                    if ("exit".equalsIgnoreCase(input)) {
                        return;
                    }

                    serialPort.writeString(String.format("%s\r\n", input), "US-ASCII");
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            serialPort.closePort();
        } catch (SerialPortException ex) {
            System.out.println(ex);
        } finally {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }

    }

}
