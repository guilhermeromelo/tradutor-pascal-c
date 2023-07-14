package gui.iftm.models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Helper {

    public static void parseTabIntoSpace(String stringFilePath) {
        Path filePath = Paths.get(stringFilePath);
        String spaces = "    ";
        String fileContent;
        try {
            fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            fileContent = fileContent.replace("\t", spaces);
            Files.write(filePath, fileContent.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
