package com.yt.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String fileName = "PDF测试.pdf";
        String content = "测试pdf";
        String result = pdfGenerationTool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}