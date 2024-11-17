package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileLineReader {

    public static void main(String[] args) {
        String filePath = "/path/to/your/file.txt";

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            System.out.println("文件内容按行解析为列表:");
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}