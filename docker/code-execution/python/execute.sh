#!/bin/bash

# Python 代码执行脚本
# 用于在隔离环境中安全执行 Python 代码

set -e

# 配置参数
TIMEOUT=${TIMEOUT:-30}
MEMORY_LIMIT=${MEMORY_LIMIT:-128}
MAX_OUTPUT_SIZE=${MAX_OUTPUT_SIZE:-1048576}  # 1MB

# 工作目录
WORK_DIR="/tmp/execution"
INPUT_DIR="$WORK_DIR/input"
OUTPUT_DIR="$WORK_DIR/output"

# 清理函数
cleanup() {
    rm -rf "$WORK_DIR"/*
    mkdir -p "$INPUT_DIR" "$OUTPUT_DIR"
}

# 执行 Python 代码
execute_python() {
    local source_file="$INPUT_DIR/main.py"
    local input_file="$INPUT_DIR/input.txt"
    local output_file="$OUTPUT_DIR/output.txt"
    local error_file="$OUTPUT_DIR/error.txt"
    local result_file="$OUTPUT_DIR/result.json"
    
    # 检查源文件是否存在
    if [ ! -f "$source_file" ]; then
        echo '{"status":"error","message":"源文件不存在","output":"","error":"main.py file not found"}' > "$result_file"
        return 1
    fi
    
    # 执行阶段
    echo "执行 Python 代码..."
    local exec_start=$(date +%s%3N)
    
    # 设置 Python 执行参数
    local python_opts="-u -W ignore::DeprecationWarning"
    
    # 执行代码
    local exit_code=0
    if [ -f "$input_file" ]; then
        timeout "$TIMEOUT"s python $python_opts "$source_file" < "$input_file" > "$output_file" 2> "$error_file" || exit_code=$?
    else
        timeout "$TIMEOUT"s python $python_opts "$source_file" > "$output_file" 2> "$error_file" || exit_code=$?
    fi
    
    local exec_end=$(date +%s%3N)
    local exec_time=$((exec_end - exec_start))
    
    # 处理执行结果
    local status="success"
    local message="执行成功"
    local output=""
    local error=""
    
    # 读取输出（限制大小）
    if [ -f "$output_file" ]; then
        output=$(head -c "$MAX_OUTPUT_SIZE" "$output_file" 2>/dev/null || echo "")
    fi
    
    # 读取错误信息
    if [ -f "$error_file" ] && [ -s "$error_file" ]; then
        error=$(head -c 1000 "$error_file" 2>/dev/null || echo "")
    fi
    
    # 根据退出码确定状态
    case $exit_code in
        0)
            status="success"
            message="执行成功"
            ;;
        124)
            status="timeout"
            message="执行超时"
            ;;
        137)
            status="memory_limit"
            message="内存超限"
            ;;
        1)
            if [[ "$error" == *"SyntaxError"* ]]; then
                status="syntax_error"
                message="语法错误"
            elif [[ "$error" == *"IndentationError"* ]]; then
                status="indentation_error"
                message="缩进错误"
            else
                status="runtime_error"
                message="运行时错误"
            fi
            ;;
        *)
            status="runtime_error"
            message="运行时错误"
            ;;
    esac
    
    # 生成结果 JSON
    cat > "$result_file" << EOF
{
    "status": "$status",
    "message": "$message",
    "output": $(echo "$output" | jq -R -s .),
    "error": $(echo "$error" | jq -R -s .),
    "execution_time": $exec_time,
    "exit_code": $exit_code
}
EOF
    
    return $exit_code
}

# 主函数
main() {
    echo "Python 代码执行器启动..."
    
    # 清理工作目录
    cleanup
    
    # 等待输入文件
    echo "等待代码文件..."
    while [ ! -f "$INPUT_DIR/main.py" ]; do
        sleep 0.1
        # 防止无限等待
        if [ $(($(date +%s) - ${START_TIME:-$(date +%s)})) -gt 60 ]; then
            echo '{"status":"error","message":"等待超时","output":"","error":"No source file received within 60 seconds"}' > "$OUTPUT_DIR/result.json"
            exit 1
        fi
    done
    
    # 执行代码
    execute_python
    
    echo "执行完成"
}

# 如果直接运行脚本
if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    START_TIME=$(date +%s)
    main "$@"
fi
