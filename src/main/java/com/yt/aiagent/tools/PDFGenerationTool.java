package com.yt.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.yt.aiagent.constant.FileConstant;
import com.yt.aiagent.oss.service.AliyunOssService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * PDF生成工具
 */
@Component
public class PDFGenerationTool {

    @Resource
    AliyunOssService aliyunOssService;

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(@ToolParam(description = "Name of the file to save the generate PDF") String fileName,
                              @ToolParam(description = "Content to be include in the PDF") String content){
        String fileDir = FileConstant.FILE_SAVE_DIR + File.separator + "pdf";
        String filePath = fileDir + File.separator + fileName;
        try {
            FileUtil.mkdir(fileDir);
            //创建PDFWriter 和 PDFDocument 对象
            try(PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)) {

                //使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light","UniGB-UCS2-H");
                document.setFont(font);
                //创建段落
                Paragraph paragraph = new Paragraph(content);
                //添加段落并关闭文档
                document.add(paragraph);
            }
            String upLoadUrl = aliyunOssService.upLoad(filePath);
            return "PDF generate successfully to: "+upLoadUrl;
        } catch (Exception e) {
            return "Error generating PDF: "+e.getMessage();
        }
    }
}
