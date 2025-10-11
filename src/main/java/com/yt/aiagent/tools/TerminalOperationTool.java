package com.yt.aiagent.tools;

import lombok.experimental.Tolerate;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 终端操作工具
 */
@Component
public class TerminalOperationTool {

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command){
        StringBuilder output = new StringBuilder();
        ProcessBuilder builder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows 系统：通过 cmd /c 执行
            builder = new ProcessBuilder("cmd", "/c", command);
        } else {
            // Linux/Mac 系统：通过 bash -c 执行（支持 shell 特性）
            builder = new ProcessBuilder("bash", "-c", command);
        }
        try {
            Process process = builder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while((line = reader.readLine()) != null){
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if(exitCode != 0){
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
