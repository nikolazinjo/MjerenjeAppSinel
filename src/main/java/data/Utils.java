package data;

import data.excel.ExcelDataManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> readResourceFile(String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        try (InputStream is = ExcelDataManager.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        }

        return list;
    }


    public static Image loadImage(String path, int width, int height) {
        try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(path)) {
            Image image = ImageIO.read(inputStream).getScaledInstance(width,height,Image.SCALE_DEFAULT);
            return image;
        } catch (Exception ignorable) { }
        return null;
    }
}
