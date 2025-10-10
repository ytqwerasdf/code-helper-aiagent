package com.yt.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.yt.aiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
@Slf4j
public class PDFWithImagesTool {

    @Tool(description = "Generate a PDF file with content and images")
    public String generatePDFWithImages(
            @ToolParam(description = "Name of the file to save the generate PDF") String fileName,
            @ToolParam(description = "Content to be include in the PDF") String content,
            @ToolParam(description = "List of image URLs to embed in PDF") String imageUrls) {

        String fileDir = FileConstant.FILE_SAVE_DIR + File.separator + "pdf";
        String filePath = fileDir + File.separator + fileName;

        try {
            FileUtil.mkdir(fileDir);

            try(PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)) {

                // 使用中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light","UniGB-UCS2-H");
                document.setFont(font);

                // 添加文本内容
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);

                // 处理图片URL列表
                if (StrUtil.isNotBlank(imageUrls)) {
                    String[] urls = imageUrls.split(",");
                    for (String url : urls) {
                        try {
                            // 下载图片到临时文件
                            String tempImagePath = downloadImageToTemp(url);
                            if (StrUtil.isNotBlank(tempImagePath)) {
                                // 添加图片到PDF
                                Image image = new Image(ImageDataFactory.create(tempImagePath));
                                image.scaleToFit(400, 300); // 设置图片大小
                                document.add(image);
                                // 删除临时文件
                                FileUtil.del(tempImagePath);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to add image from URL: " + url, e);
                        }
                    }
                }
            }
            return "PDF with images generated successfully to: " + filePath;
        } catch (Exception e) {
            return "Error generating PDF with images: " + e.getMessage();
        }
    }

    private String downloadImageToTemp(String imageUrl) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
            String tempPath = tempDir + File.separator + fileName;

            HttpUtil.downloadFile(imageUrl, new File(tempPath));
            return tempPath;
        } catch (Exception e) {
            return null;
        }
    }
}
