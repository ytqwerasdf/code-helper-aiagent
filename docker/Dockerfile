# 使用 ubuntu 基础镜像
FROM ubuntu:22.04

# 安装基础工具（含 xz-utils 支持 .xz 解压）
RUN apt-get update && apt-get install -y wget curl tar xz-utils && rm -rf /var/lib/apt/lists/*

# 安装 Node.js（新增 npx 软链接，关键修复）
COPY node-v22.14.0-linux-x64.tar.xz /tmp/
RUN tar -xJf /tmp/node-v22.14.0-linux-x64.tar.xz -C /usr/local/ && \
    # 为 node、npm、npx 分别创建软链接（确保路径正确）
    ln -s /usr/local/node-v22.14.0-linux-x64/bin/node /usr/bin/node && \
    ln -s /usr/local/node-v22.14.0-linux-x64/bin/npm /usr/bin/npm && \
    ln -s /usr/local/node-v22.14.0-linux-x64/bin/npx /usr/bin/npx && \
    # 验证 npx 是否可用（提前排查问题）
    npx --version && \
    rm /tmp/node-v22.14.0-linux-x64.tar.xz

# 验证 Node.js 环境（确认 node/npm/npx 均正常）
RUN echo "Node.js 版本：$(node -v)" && \
    echo "npm 版本：$(npm -v)" && \
    echo "npx 版本：$(npx --version)"

# 复制并安装 JRE
COPY openjdk21-jre.tar.gz /tmp/
RUN tar -zxvf /tmp/openjdk21-jre.tar.gz -C /usr/local/ && \
    mv /usr/local/jdk-21* /usr/local/openjdk21-jre && \
    rm /tmp/openjdk21-jre.tar.gz

# 设置 Java 环境（确保与 Node 环境不冲突）
ENV JAVA_HOME=/usr/local/openjdk21-jre
ENV PATH=$JAVA_HOME/bin:$PATH
RUN java -version

# 提前安装依赖包（用 npm 全局安装，避免 npx 临时下载）
RUN npm install -g @amap/amap-maps-mcp-server

# 验证依赖包是否安装成功（确认路径）
RUN echo "全局 Node 包路径：$(npm root -g)" && \
    ls -l $(npm root -g)/@amap/amap-maps-mcp-server  # 确认包存在

# 创建应用用户（带主目录，避免权限问题）
RUN groupadd -r appuser && useradd -r -g appuser -m -d /home/appuser appuser

# 工作目录与应用部署
WORKDIR /app
COPY agent-backend/ai-agent-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到普通用户（验证用户下 npx 是否可用）
USER appuser
RUN npx --version  # 确保普通用户也能执行 npx

# 暴露端口
EXPOSE 8123

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8123/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
