package com.yt.aiagent.constant;


import java.io.File;

/**
 * 文件常量
 */
public interface FileConstant {

    /**
     * 文件保存目录
     */
    String FILE_SAVE_DIR = System.getProperty("user.dir")+ File.separator+"tmp";

}
