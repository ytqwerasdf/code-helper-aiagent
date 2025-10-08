package com.yt.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test1.txt";
        String read = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull(read);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test1.txt";
        String testWriteFile = fileOperationTool.writeFile(fileName, "testWriteFile");
        Assertions.assertNotNull(testWriteFile);
    }
}