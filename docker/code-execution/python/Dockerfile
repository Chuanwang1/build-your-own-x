# Python 代码执行环境 Dockerfile
FROM python:3.11-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    timeout \
    jq \
    && rm -rf /var/lib/apt/lists/*

# 创建执行用户（非 root 用户，提高安全性）
RUN groupadd -r coderunner && useradd -r -g coderunner coderunner

# 设置工作目录
WORKDIR /tmp/execution

# 创建必要的目录
RUN mkdir -p /tmp/execution/input /tmp/execution/output \
    && chown -R coderunner:coderunner /tmp/execution

# 安装常用的 Python 包
RUN pip install --no-cache-dir \
    numpy==1.24.3 \
    pandas==2.0.3 \
    matplotlib==3.7.2 \
    requests==2.31.0 \
    beautifulsoup4==4.12.2

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
ENV PYTHONPATH=/tmp/execution/input
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

# 默认命令
CMD ["/usr/local/bin/execute.sh"]
