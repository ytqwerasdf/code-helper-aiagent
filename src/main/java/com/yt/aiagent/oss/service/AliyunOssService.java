package com.yt.aiagent.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.Date;

import static com.alibaba.nacos.client.aliyun.sample.KmsV3Sample.content;

@Service
public class AliyunOssService {

    @Resource
    private OSS ossClient;

    @Value("${oss.aliyun.bucketName}")
    private String bucketName;


    /**
     * 上传文件
     */
    public String upLoad(String filePath){
        File file = new File(filePath);
        // 验证文件是否存在且可读取
        if (!file.exists() || !file.canRead()) {
            throw new RuntimeException("文件不存在或无法读取: " + filePath);
        }
        try {
            String fileName =file.getName();
            String objectName = "agentGenerate/"+fileName;
            try (FileInputStream fis = new FileInputStream(file)) {
                PutObjectResult result = ossClient.putObject(bucketName, objectName, fis);
            }
            //  生成临时访问 URL（有效期 1 天）
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000*24);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
            request.setExpiration(expiration);
            URL presignedUrl = ossClient.generatePresignedUrl(request);

            //  返回带签名参数的 URL
            return presignedUrl.toString();
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败"+e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param objectName
     * @return
     */
    public OSSObject downLoad(String objectName){
        OSSObject ossObject = ossClient.getObject(bucketName, objectName);
        return ossObject;
    }

    /**
     * 列出所有文件
     * @return
     */
    public ObjectListing listFile(){
        System.out.println("4. 列出 Bucket 中的文件：");
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        for (
                OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + " (大小 = " + objectSummary.getSize() + ")");
        }
        return objectListing;
    }


    /**
     *删除文件
     * @param objectName
     */
    public void delete(String objectName){
        ossClient.deleteObject(bucketName, objectName);
        System.out.println("5. 文件 " + objectName + " 删除成功。");
    }



}
