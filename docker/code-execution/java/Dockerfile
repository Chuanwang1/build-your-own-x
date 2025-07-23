# Java 代码执行环境 Dockerfile
FROM openjdk:17-jdk-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    timeout \
    && rm -rf /var/lib/apt/lists/*

# 创建执行用户（非 root 用户，提高安全性）
RUN groupadd -r coderunner && useradd -r -g coderunner coderunner

# 设置工作目录
WORKDIR /tmp/execution

# 创建必要的目录
RUN mkdir -p /tmp/execution/input /tmp/execution/output \
    && chown -R coderunner:coderunner /tmp/execution

# 设置资源限制
RUN echo "coderunner soft nproc 32" >> /etc/security/limits.conf \
    && echo "coderunner hard nproc 32" >> /etc/security/limits.conf \
    && echo "coderunner soft nofile 64" >> /etc/security/limits.conf \
    && echo "coderunner hard nofile 64" >> /etc/security/limits.conf

# 复制执行脚本
COPY execute.sh /usr/local/bin/execute.sh
RUN chmod +x /usr/local/bin/execute.sh

# 切换到执行用户
USER coderunner

# 设置环境变量
ENV JAVA_OPTS="-Xms64m -Xmx128m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# 默认命令
CMD ["/usr/local/bin/execute.sh"]
