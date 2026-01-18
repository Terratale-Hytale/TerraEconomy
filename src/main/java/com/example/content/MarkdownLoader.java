package com.example.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MarkdownLoader {

    public MarkdownLoader() {}

    public static List<String> loadLines(Path flie) throws IOException {
        return Files.readAllLines(flie);
    }

}
