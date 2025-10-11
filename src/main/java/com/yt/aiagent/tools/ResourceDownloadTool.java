package com.yt.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.yt.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 资源下载工具
 */
@Component
public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the download resource") String fileName){
        String fileDir = FileConstant.FILE_SAVE_DIR + File.separator+"download";
        String filePath = fileDir+File.separator+fileName;
        try {
            //创建目录
            FileUtil.mkdir(fileDir);
            //下载资源
            HttpUtil.downloadFile(url,new File(filePath));
            return "Resource download successfully to: "+filePath;
        } catch (Exception e) {
            return "Error download resource: "+e.getMessage();
        }
    }
}
